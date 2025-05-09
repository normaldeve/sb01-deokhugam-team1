package com.codeit.duckhu.domain.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.duckhu.domain.review.entity.PopularReview;
import com.codeit.duckhu.domain.review.entity.Review;
import com.codeit.duckhu.global.type.Direction;
import com.codeit.duckhu.global.type.PeriodType;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
public class PopularReviewRepositoryTest {

  @Autowired private PopularReviewRepository popularReviewRepository;
  @Autowired private ReviewRepository reviewRepository;

  @Nested
  @DisplayName("인기 리뷰 커서페이지네이션 테스트")
  class PopularReviewCursorPaginationTest {

    @Test
    @DisplayName("커서 기반 페이지네이션으로 인기 리뷰 조회")
    void findPopularReviewWithCursor_success() {
      // Given
      Instant now = Instant.now();

      Review reviewEntity1 = Review.builder().content("리뷰 내용 1").rating(4).build();
      reviewRepository.save(reviewEntity1);

      Review reviewEntity2 = Review.builder().content("리뷰 내용 2").rating(3).build();
      reviewRepository.save(reviewEntity2);

      reviewRepository.flush();

      PopularReview review1 =
          PopularReview.builder()
              .review(reviewEntity1)
              .rank(1)
              .commentCount(3)
              .reviewRating(4.0)
              .likeCount(4)
              .period(PeriodType.DAILY)
              .score(50.0)
              .build();

      PopularReview review2 =
          PopularReview.builder()
              .review(reviewEntity2)
              .rank(2)
              .commentCount(1)
              .reviewRating(3.0)
              .likeCount(2)
              .period(PeriodType.DAILY)
              .score(40.0)
              .build();

      popularReviewRepository.save(review1);
      popularReviewRepository.save(review2);

      popularReviewRepository.flush();

      // When
      List<PopularReview> result =
          popularReviewRepository.findReviewsWithCursor(
              PeriodType.DAILY, Direction.ASC, null, now, 10);

      // Then
      assertThat(result).hasSize(2);
      assertThat(result.get(0).getRank()).isEqualTo(1);
    }

    @Test
    @DisplayName("기간별 인기 리뷰 수 확인")
    void countByPeriod_success() {
      // Given
      Instant from = Instant.now().minusSeconds(864000); // 하루 전

      Review reviewEntity = Review.builder().content("리뷰 내용").rating(4).build();
      reviewRepository.save(reviewEntity);
      reviewRepository.flush();

      PopularReview review =
          PopularReview.builder()
              .review(reviewEntity)
              .rank(1)
              .commentCount(3)
              .reviewRating(4.0)
              .likeCount(4)
              .period(PeriodType.WEEKLY)
              .score(50.0)
              .build();

      popularReviewRepository.save(review);
      popularReviewRepository.flush();

      // When
      long count = popularReviewRepository.countByPeriodSince(PeriodType.WEEKLY, from);

      // Then
      assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("기간별 인기 리뷰 삭제")
    void deleteByPeriod_success() {
      // Given
      Review reviewEntity = Review.builder().content("리뷰 내용").rating(4).build();

      reviewRepository.save(reviewEntity);
      reviewRepository.flush();

      PopularReview review =
          PopularReview.builder()
              .review(reviewEntity)
              .rank(1)
              .commentCount(3)
              .reviewRating(4.0)
              .likeCount(4)
              .period(PeriodType.DAILY)
              .score(50.0)
              .build();

      popularReviewRepository.save(review);
      popularReviewRepository.flush();

      // When
      popularReviewRepository.deleteByPeriod(PeriodType.DAILY);

      // Then
      List<PopularReview> all = popularReviewRepository.findAll();
      assertThat(all).isEmpty();
    }

    @Test
    @DisplayName("cursor만 있고 after가 없는 경우")
    void findPopularReview_cursorWithoutAfter() {
      List<PopularReview> result = popularReviewRepository.findReviewsWithCursor(
          PeriodType.WEEKLY,
          Direction.ASC,
          "2",
          null,  // after가 null
          10
      );
      assertThat(result).isNotNull(); // 예외 없이 동작하는지만 확인
    }

    @Test
    @DisplayName("정렬 방향이 DESC인 경우")
    void findPopularReview_descDirection() {
      List<PopularReview> result = popularReviewRepository.findReviewsWithCursor(
          PeriodType.DAILY,
          Direction.DESC,
          null,
          Instant.now(),
          10
      );
      assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("period가 null인 경우")
    void findPopularReviewWithoutPeriod() {
      List<PopularReview> result =
          popularReviewRepository.findReviewsWithCursor(
              null,  // period
              Direction.ASC,
              null,
              Instant.now(),
              10
          );
      assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("cursor만 있고 after는 null인 경우")
    void findPopularReviewWithCursorOnly() {
      List<PopularReview> result =
          popularReviewRepository.findReviewsWithCursor(
              PeriodType.DAILY,
              Direction.DESC,
              "1",     // cursor만 존재
              null,    // after 없음
              10
          );
      assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("정렬 방향이 DESC일 경우")
    void findPopularReviewDescDirection() {
      List<PopularReview> result =
          popularReviewRepository.findReviewsWithCursor(
              PeriodType.WEEKLY,
              Direction.DESC,
              null,
              Instant.now(),
              5
          );
      assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("커서 조건이 있고 정렬 방향이 ASC인 경우")
    void findPopularReviewWithCursor_ASC() {
      // Given
      Instant now = Instant.now();
      String cursor = "1";
      Direction direction = Direction.ASC;

      // When
      List<PopularReview> result =
          popularReviewRepository.findReviewsWithCursor(
              PeriodType.DAILY, direction, cursor, now, 10);

      // Then
      assertThat(result).isNotNull();
    }
  }
}
