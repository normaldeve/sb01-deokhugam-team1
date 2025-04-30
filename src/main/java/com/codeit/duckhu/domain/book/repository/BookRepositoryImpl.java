package com.codeit.duckhu.domain.book.repository;

import com.codeit.duckhu.domain.book.entity.Book;
import com.codeit.duckhu.domain.book.entity.QBook;
import com.codeit.duckhu.global.type.Direction;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  private final QBook book = QBook.book;

  @Override
  public List<Book> searchBooks(
      String keyword,
      String orderBy,
      Direction direction,
      String cursor,
      Instant after,
      int limit) {
    BooleanBuilder condition = new BooleanBuilder();

    // 키워드 검색 -> 제목, 저자, isbn으로 검색
    if (StringUtils.hasText(keyword)) {
      condition.andAnyOf(
          book.title.containsIgnoreCase(keyword),
          book.author.containsIgnoreCase(keyword),
          book.isbn.containsIgnoreCase(keyword));
    }

    // 논리 삭제 제외
    condition.and(book.isDeleted.eq(false));

    // 커서 페이지네이션 조건 추가하기
    if (cursor != null && after != null) {
      condition.and(getCursorCondition(orderBy, cursor, after, isAsc(direction)));
    }

    // 정렬 조건 추가하기
    List<OrderSpecifier<?>> orderSpecifiers = buildOrderSpecifiers(orderBy, isAsc(direction));

    return queryFactory
        .selectFrom(book)
        .where(condition)
        .orderBy(orderSpecifiers.toArray(OrderSpecifier[]::new))
        .limit(limit)
        .fetch();
  }

  private boolean isAsc(Direction direction) {
    return "ASC".equalsIgnoreCase(String.valueOf(direction));
  }

  private BooleanBuilder getCursorCondition(
      String sortBy, String cursor, Instant after, boolean isAsc) {
    BooleanBuilder builder = new BooleanBuilder();

    // 정렬 필드 -> 제목, 평점, 출판일, 리뷰수
    switch (sortBy) {
      case "title" -> {
        builder.and(
            isAsc
                ? book.title.gt(cursor).or(book.title.eq(cursor).and(book.createdAt.gt(after)))
                : book.title.lt(cursor).or(book.title.eq(cursor).and(book.createdAt.lt(after))));
      }
      case "rating" -> {
        Double rating = Double.parseDouble(cursor);
        builder.and(
            isAsc
                ? book.rating.gt(rating).or(book.rating.eq(rating).and(book.createdAt.gt(after)))
                : book.rating.lt(rating).or(book.rating.eq(rating).and(book.createdAt.lt(after))));
      }
      case "reviewCount" -> {
        Integer reviewCount = Integer.parseInt(cursor);
        builder.and(
            isAsc
                ? book.reviewCount
                    .gt(reviewCount)
                    .or(book.reviewCount.eq(reviewCount).and(book.createdAt.gt(after)))
                : book.reviewCount
                    .lt(reviewCount)
                    .or(book.reviewCount.eq(reviewCount).and(book.createdAt.lt(after))));
      }
      case "publishedDate" -> {
        builder.and(
            isAsc
                ? book.publishedDate
                    .after(java.time.LocalDate.parse(cursor))
                    .or(
                        book.publishedDate
                            .eq(java.time.LocalDate.parse(cursor))
                            .and(book.createdAt.gt(after)))
                : book.publishedDate
                    .before(java.time.LocalDate.parse(cursor))
                    .or(
                        book.publishedDate
                            .eq(java.time.LocalDate.parse(cursor))
                            .and(book.createdAt.lt(after))));
      }
      default -> builder.and(isAsc ? book.createdAt.gt(after) : book.createdAt.lt(after));
    }

    return builder;
  }

  // 정렬 기준 필드 + createdAt을 함께 적용한 Order By 조건을 생성함
  private List<OrderSpecifier<?>> buildOrderSpecifiers(String sortBy, boolean isAsc) {
    List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

    switch (sortBy) {
      case "title" -> orderSpecifiers.add(isAsc ? book.title.asc() : book.title.desc());
      case "publishedDate" ->
          orderSpecifiers.add(isAsc ? book.publishedDate.asc() : book.publishedDate.desc());
      case "rating" -> orderSpecifiers.add(isAsc ? book.rating.asc() : book.rating.desc());
      case "reviewCount" ->
          orderSpecifiers.add(isAsc ? book.reviewCount.asc() : book.reviewCount.desc());
      default -> orderSpecifiers.add(isAsc ? book.createdAt.asc() : book.createdAt.desc());
    }

    orderSpecifiers.add(isAsc ? book.createdAt.asc() : book.createdAt.desc());
    return orderSpecifiers;
  }
}
