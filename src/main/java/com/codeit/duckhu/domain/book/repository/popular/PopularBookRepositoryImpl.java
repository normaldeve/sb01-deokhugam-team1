package com.codeit.duckhu.domain.book.repository.popular;

import com.codeit.duckhu.domain.book.entity.PopularBook;
import com.codeit.duckhu.domain.book.entity.QPopularBook;
import com.codeit.duckhu.global.type.Direction;
import com.codeit.duckhu.global.type.PeriodType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PopularBookRepositoryImpl implements PopularBookRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<PopularBook> searchByPeriodWithCursorPaging(
      PeriodType period, Direction direction, String cursor, Instant after, int limit) {

    QPopularBook popularBook = QPopularBook.popularBook;

    BooleanBuilder condition = new BooleanBuilder();
    condition.and(popularBook.period.eq(period));

    Instant now = Instant.now();
    Instant from = period.toStartInstant(now);
    condition.and(popularBook.createdAt.goe(from));

    boolean isAsc = "ASC".equalsIgnoreCase(String.valueOf(direction));

    if (cursor != null && after != null) {
      int rank = Integer.parseInt(cursor);

      BooleanBuilder cursorCondition = new BooleanBuilder();
      if (isAsc) {
        cursorCondition.and(
            popularBook
                .rank
                .gt(rank)
                .or(popularBook.rank.eq(rank).and(popularBook.createdAt.gt(after))));
      } else {
        cursorCondition.and(
            popularBook
                .rank
                .lt(rank)
                .or(popularBook.rank.eq(rank).and(popularBook.createdAt.lt(after))));
      }
      condition.and(cursorCondition);
    }

    OrderSpecifier<?>[] orderSpecifiers =
        isAsc
            ? new OrderSpecifier[] {popularBook.rank.asc(), popularBook.createdAt.asc()}
            : new OrderSpecifier[] {popularBook.rank.desc(), popularBook.createdAt.desc()};

    return queryFactory
        .selectFrom(popularBook)
        .where(condition)
        .orderBy(orderSpecifiers)
        .limit(limit)
        .fetch();
  }
}
