package com.codeit.duckhu.domain.review.service.impl;

import static com.codeit.duckhu.domain.comment.domain.QComment.comment;
import static com.codeit.duckhu.domain.review.entity.QLikedUserId.likedUserId;
import static com.codeit.duckhu.domain.review.entity.QReview.review;

import com.codeit.duckhu.domain.notification.service.NotificationService;
import com.codeit.duckhu.domain.review.entity.PopularReview;
import com.codeit.duckhu.domain.review.entity.Review;
import com.codeit.duckhu.domain.review.repository.PopularReviewRepository;
import com.codeit.duckhu.global.type.PeriodType;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인기 리뷰 점수 계산 및 랭킹 업데이트를 위한 배치 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PopularReviewBatchService {

  private final PopularReviewRepository popularReviewRepository;

  private final JPAQueryFactory queryFactory;
  private final NotificationService notificationService;
  private final MeterRegistry meterRegistry;

  /**
   * 매일 새벽 12시에 실행
   */
  @Scheduled(cron = "0 0 0 * * ?")
  @Transactional
  public void calculatePopularReviews() {
    log.info("인기 리뷰 점수 계산 및 랭킹 업데이트");
    Instant now = Instant.now();

    for (PeriodType period : PeriodType.values()) {
      // ALL_TIME은 startTime을 null로 설정하여 전체 기간을 대상으로 합니다.
      Instant startTime = period.toStartInstant(now);
      log.info("{} 기간 인기 리뷰 계산 시작 (시작 시간: {})", period, startTime);

      try {
        // DB에서 기간 내 활동(좋아요, 댓글)이 있는 리뷰 데이터 조회
        List<PopularReviewData> scoredReviews = fetchAndScoreReviewsForPeriod(period, startTime,
            now);

        // 점수 기준 내림차순 정렬
        scoredReviews.sort(Comparator.comparingDouble(PopularReviewData::getScore).reversed());
        log.info("{} 기간 처리 대상 리뷰 수 : {}", period, scoredReviews.size());

        // 기존 데이터 삭제
        popularReviewRepository.deleteByPeriod(period);
        log.info("{} 기간 기존 데이터 삭제 완료", period);

        // PopularReview 엔티티 생성, 저장
        if (!scoredReviews.isEmpty()) {
          List<PopularReview> popularReviewsToSave = new ArrayList<>();
          int rank = 1;
          for (PopularReviewData data : scoredReviews) {
            PopularReview popularReview = PopularReview.builder()
                .review(data.getReview())
                .period(period)
                .score(data.getScore())
                .rank(rank)
                .likeCount(data.getLikesInPeriod())
                .commentCount(data.getCommentsInPeriod())
                .reviewRating((double) data.getReview().getRating())
                .build();
            popularReviewsToSave.add(popularReview);

            // 10위 안에 드는 경우 알림 생성
            if (rank <= 10) {
              UUID reviewId   = data.getReview().getId();
              UUID receiverId = data.getReview().getUser().getId();
              notificationService.createNotifyByPopularReview(
                  reviewId, receiverId, period, rank
              );
            }
            rank++;

          }
          popularReviewRepository.saveAll(popularReviewsToSave);
          log.info("{} 기간 인기 리뷰 {}개 저장 완료", period, popularReviewsToSave.size());


          meterRegistry.counter("batch.powerReview.complete", "period", period.name()).increment();
        } else {
          log.info("{} 기간 처리 대상 리뷰 없음", period);

          meterRegistry.counter("batch.powerReview.noReviewInPeriod", "period", period.name()).increment();
        }
      } catch (Exception e) {
        log.debug("{} 인기 리뷰 기간 계산 오류 발생", period, e);

        meterRegistry.counter("batch.powerReview.failure", "period", period.name()).increment();
      }
    }
    log.info("인기 리뷰 업데이트 완료");
  }

  /**
   * Period동안 좋아요 또는 댓글이 1개 이상인 리뷰 정보를 DB에서 조회
   *
   * @param period
   * @param startTime
   * @param now
   * @return
   */
  private List<PopularReviewData> fetchAndScoreReviewsForPeriod(PeriodType period,
      Instant startTime, Instant now) {

    // JOIN 조건 생성
    BooleanExpression likeJoinCondition = startTime !=
        null ? likedUserId.createdAt.between(startTime, now) : null;

    BooleanExpression commentJoinCondition = comment.isDeleted.eq(false);
    if (startTime != null) {
      commentJoinCondition = commentJoinCondition.and(comment.createdAt.between(startTime, now));
    }

    // 쿼리 실행
    List<Tuple> results = queryFactory
        .select(
            review,
            likedUserId.id.countDistinct(),
            comment.id.countDistinct()
        )
        .from(review)
        .leftJoin(review.likedUserIds, likedUserId)
               .on(likeJoinCondition)
               .on(likeJoinCondition != null
                   ? likeJoinCondition
                   : likedUserId.isNotNull())
        .leftJoin(review.comments, comment).on(commentJoinCondition)
        .where(review.isDeleted.eq(false))
        .groupBy(review)
        .having(
            likedUserId.id.countDistinct().gt(0L)
                .or(comment.id.countDistinct().gt(0L))
        )
        .fetch();

    // 조회 결과를 PopularReviewData로 변환 및 계산
    return results.stream().map(tuple -> {
      Review currentReview = tuple.get(review);

      int likesInPeriod = tuple.get(1, Long.class) != null ?
          tuple.get(1, Long.class).intValue() : 0;
      int commentsInPeriod = tuple.get(2, Long.class) != null ?
          tuple.get(2, Long.class).intValue() : 0;

      // 점수 계산
      double score = (likesInPeriod * 0.3) + (commentsInPeriod * 0.7);

      return new PopularReviewData(currentReview, score, likesInPeriod, commentsInPeriod);
    }).collect(Collectors.toList());
  }

  @RequiredArgsConstructor
  @Getter
  private static class PopularReviewData {
    private final Review review;
    private final double score;
    private final int likesInPeriod;
    private final int commentsInPeriod;
  }
} 