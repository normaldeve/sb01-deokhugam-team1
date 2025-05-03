package com.codeit.duckhu.domain.book.repository;

import com.codeit.duckhu.domain.book.entity.PopularBook;
import com.codeit.duckhu.domain.book.repository.popular.PopularBookRepositoryCustom;
import com.codeit.duckhu.domain.book.repository.popular.PopularBookRepositoryImpl;
import com.codeit.duckhu.domain.review.repository.TestJpaConfig;
import com.codeit.duckhu.global.type.Direction;
import com.codeit.duckhu.global.type.PeriodType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.test.context.jdbc.Sql;

@ActiveProfiles("test")
@DataJpaTest
@Import({PopularBookRepositoryImpl.class, TestJpaConfig.class})
@Sql("/books.sql")
public class PopularBookRepositoryImplTest {

  @Autowired
  private PopularBookRepositoryCustom popularBookRepository;

  @Test
  @DisplayName("DAILY 기간의 인기 도서 목록을 ASC 정렬로 모두 조회한다")
  void searchDailyBooks() {
    // when
    List<PopularBook> daily = popularBookRepository.searchByPeriodWithCursorPaging(
        PeriodType.DAILY, Direction.ASC, null, null, 10);

    // then
    assertThat(daily).hasSize(3); // DAILY 데이터 3건
    assertThat(daily).extracting(PopularBook::getPeriod)
        .allMatch(p -> p == PeriodType.DAILY);
  }

  @Test
  @DisplayName("WEEKLY 기간의 인기 도서 목록을 ASC 정렬로 모두 조회한다")
  void searchWeeklyBooks() {
    // when
    List<PopularBook> weekly = popularBookRepository.searchByPeriodWithCursorPaging(
        PeriodType.WEEKLY, Direction.ASC, null, null, 10);

    // then
    assertThat(weekly).hasSize(3); // WEEKLY 데이터 3건
    assertThat(weekly).extracting(PopularBook::getPeriod)
        .allMatch(p -> p == PeriodType.WEEKLY);
  }

  @Test
  @DisplayName("MONTHLY 기간의 인기 도서 목록을 ASC 정렬로 모두 조회한다")
  void searchMonthlyBooks() {
    // when
    List<PopularBook> monthly = popularBookRepository.searchByPeriodWithCursorPaging(
        PeriodType.MONTHLY, Direction.ASC, null, null, 10);

    // then
    assertThat(monthly).hasSize(2); // MONTHLY 데이터 2건
    assertThat(monthly).extracting(PopularBook::getPeriod)
        .allMatch(p -> p == PeriodType.MONTHLY);
  }

  @Test
  @DisplayName("ALL_TIME 기간의 인기 도서 목록을 ASC 정렬로 모두 조회한다")
  void searchAllTimeBooks() {
    // when
    List<PopularBook> allTime = popularBookRepository.searchByPeriodWithCursorPaging(
        PeriodType.ALL_TIME, Direction.ASC, null, null, 10);

    // then
    assertThat(allTime).hasSize(2); // ALL_TIME 데이터 2건
    assertThat(allTime).extracting(PopularBook::getPeriod)
        .allMatch(p -> p == PeriodType.ALL_TIME);
  }

  @Test
  @DisplayName("ASC 정렬: rank=5, createdAt=1분 전 커서 기준 이후 데이터만 조회")
  void searchAscWithCursor() {
    // given
    String cursor = "5";
    Instant after = Instant.now().minusSeconds(60); // 커서의 createdAt 보다 더 이전으로 가정

    // when
    List<PopularBook> results = popularBookRepository.searchByPeriodWithCursorPaging(
        PeriodType.WEEKLY,
        Direction.ASC,
        cursor,
        after,
        10
    );

    // then
    assertThat(results).isNotEmpty();
    for (PopularBook book : results) {
      boolean rankValid = book.getRank() > 5;
      boolean sameRankWithLaterCreated = book.getRank() == 5 && book.getCreatedAt().isAfter(after);
      assertThat(rankValid || sameRankWithLaterCreated)
          .as("rank > 5 or (rank == 5 && createdAt > after)")
          .isTrue();
    }
  }

  @Test
  @DisplayName("DESC 정렬: rank=5, createdAt=1분 전 커서 기준 이전 데이터만 조회")
  void searchDescWithCursor() {
    // given
    String cursor = "5";
    Instant after = Instant.now().minusSeconds(60);

    // when
    List<PopularBook> results = popularBookRepository.searchByPeriodWithCursorPaging(
        PeriodType.WEEKLY,
        Direction.DESC,
        cursor,
        after,
        10
    );

    // then
    assertThat(results).isNotEmpty();
    for (PopularBook book : results) {
      boolean rankValid = book.getRank() < 5;
      boolean sameRankWithEarlierCreated = book.getRank() == 5 && book.getCreatedAt().isBefore(after);
      assertThat(rankValid || sameRankWithEarlierCreated)
          .as("rank < 5 or (rank == 5 && createdAt < after)")
          .isTrue();
    }
  }

}
