package com.codeit.duckhu.domain.review.repository.custom.impl;

import com.codeit.duckhu.domain.review.entity.QReview;
import com.codeit.duckhu.domain.review.entity.Review;
import com.codeit.duckhu.domain.review.repository.custom.ReviewRepositoryCustom;
import com.codeit.duckhu.global.type.Direction;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  QReview review = QReview.review;

  @Override
  public List<Review> findReviewsWithCursor(
      String keyword,
      String orderBy,
      Direction direction,
      UUID userId,
      UUID bookId,
      String cursor,
      Instant after,
      int size) {

    BooleanBuilder booleanBuilder = new BooleanBuilder();

    // 키워드 검색 - 닉네임, 내용, 도서 제목
    if (StringUtils.hasText(keyword)) {
      booleanBuilder.andAnyOf(
          review.user.nickname.containsIgnoreCase(keyword),
          review.content.containsIgnoreCase(keyword),
          review.book.title.containsIgnoreCase(keyword));
    }

    // 작성자 ID 필터링
    if (userId != null) {
      booleanBuilder.and(review.user.id.eq(userId));
    }

    // 도서 ID 필터링
    if (bookId != null) {
      booleanBuilder.and(review.book.id.eq(bookId));
    }

    booleanBuilder.and(review.isDeleted.eq(false));

    if (cursor != null && after != null) {
      booleanBuilder.and(getCursorCondition(orderBy, cursor, after, isAsc(direction)));
    }

    List<OrderSpecifier<?>> orderSpecifiers = getOrderSpecifiers(orderBy, isAsc(direction));

    return queryFactory
        .selectFrom(review)
        .where(booleanBuilder)
        .orderBy(orderSpecifiers.toArray(OrderSpecifier[]::new))
        .limit(size)
        .fetch();
  }

  private boolean isAsc(Direction direction) {
    return "ASC".equalsIgnoreCase(String.valueOf(direction));
  }

  // 정렬 조건 필드 - createdAt, rating
  private BooleanBuilder getCursorCondition(
      String orderBy, String cursor, Instant after, boolean isAsc) {
    BooleanBuilder condition = new BooleanBuilder();

    if (orderBy.equals("createdAt")) {
      if (isAsc) {
        condition.and(
            review
                .createdAt
                .gt(after)
                .or(review.createdAt.eq(after).and(review.id.gt(UUID.fromString(cursor)))));
      } else {
        condition.and(
            review
                .createdAt
                .lt(after)
                .or(review.createdAt.eq(after).and(review.id.lt(UUID.fromString(cursor)))));
      }
    } else if (orderBy.equals("rating")) {
      int cursorRating = Integer.parseInt(cursor);
      if (isAsc) {
        condition.and(
            review
                .rating
                .gt(cursorRating)
                .or(review.rating.eq(cursorRating).and(review.createdAt.gt(after))));
      } else {
        condition.and(
            review
                .rating
                .lt(cursorRating)
                .or(review.rating.eq(cursorRating).and(review.createdAt.lt(after))));
      }
    }
    return condition;
  }

  private List<OrderSpecifier<?>> getOrderSpecifiers(String orderBy, boolean isAsc) {
    List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

    // 주요 정렬 조건 - createdAt or rating
    if ("rating".equals(orderBy)) {
      orderSpecifiers.add(isAsc ? review.rating.asc() : review.rating.desc());

      // 평점이 같은 경우 생성 시간 정렬
      orderSpecifiers.add(isAsc ? review.createdAt.asc() : review.createdAt.desc());

    } else {

      // 기본 값 정렬
      orderSpecifiers.add(isAsc ? review.createdAt.asc() : review.createdAt.desc());
    }

    orderSpecifiers.add(isAsc ? review.id.asc() : review.id.desc());

    return orderSpecifiers;
  }
}
