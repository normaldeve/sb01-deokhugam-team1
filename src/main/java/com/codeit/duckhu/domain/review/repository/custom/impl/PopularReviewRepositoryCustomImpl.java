package com.codeit.duckhu.domain.review.repository.custom.impl;

import com.codeit.duckhu.domain.review.entity.PopularReview;
import com.codeit.duckhu.domain.review.entity.QPopularReview;
import com.codeit.duckhu.domain.review.repository.custom.PopularReviewRepositoryCustom;
import com.codeit.duckhu.global.type.Direction;
import com.codeit.duckhu.global.type.PeriodType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class PopularReviewRepositoryCustomImpl implements PopularReviewRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  private final QPopularReview review = QPopularReview.popularReview;

  @Override
  public List<PopularReview> findReviewsWithCursor(PeriodType period, Direction direction,
      String cursor, Instant after, int size) {

    BooleanBuilder booleanBuilder = new BooleanBuilder();

    // null 체크
    if (period != null) {
      booleanBuilder.and(review.period.eq(period));
    }

    // 현재 시간 기준으로 시작 시간 계산
    Instant now = Instant.now();
    if(period != null) {
      Instant startTime = period.toStartInstant(now);
      booleanBuilder.and(review.createdAt.goe(startTime));
    }

    boolean isAsc = direction == Direction.ASC;

    // 커서 조건 추가
    if (cursor != null && after != null) {
      int rank = Integer.parseInt(cursor);

      BooleanBuilder cursorCondition = new BooleanBuilder();

      if (isAsc) {
        cursorCondition.and(
            review
                .rank
                .gt(rank)
                .or(review.rank.eq(rank).and(review.createdAt.gt(after))));
      } else {
        cursorCondition.and(
            review
                .rank
                .lt(rank)
                .or(review.rank.eq(rank).and(review.createdAt.lt(after))));
      }
      booleanBuilder.and(cursorCondition);
    }

    // 정렬 조건 설정
    OrderSpecifier<?>[] orderSpecifiers = isAsc
            ? new OrderSpecifier[]{review.rank.asc(), review.createdAt.asc()}
            : new OrderSpecifier[]{review.rank.desc(), review.createdAt.desc()};

    return queryFactory
        .selectFrom(review)
        .where(booleanBuilder)
        .orderBy(orderSpecifiers)
        .limit(size)
        .fetch();
  }

  @Override
  public long countByPeriodSince(PeriodType period, Instant from) {
    BooleanBuilder booleanBuilder = new BooleanBuilder();

    if (period != null) {
      booleanBuilder.and(review.period.eq(period));
    }

    booleanBuilder.and(review.createdAt.goe(from));

    return queryFactory
        .select(review.count())
        .from(review)
        .where(booleanBuilder)
        .fetchOne();
  }

  @Override
  @Transactional
  public void deleteByPeriod(PeriodType period) {
    if (period != null) {
      long deletedCount = queryFactory
          .delete(review)
          .where(review.period.eq(period))
          .execute();
      log.info("기간별 인기 리뷰 삭제 완료 - 기간 : {}, 삭제된 수 : {}", period, deletedCount);
    }
  }
}
