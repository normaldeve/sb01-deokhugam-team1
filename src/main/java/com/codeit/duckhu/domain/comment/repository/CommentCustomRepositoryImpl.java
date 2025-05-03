package com.codeit.duckhu.domain.comment.repository;

import com.codeit.duckhu.domain.comment.domain.Comment;
import com.codeit.duckhu.domain.comment.domain.QComment;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@RequiredArgsConstructor
public class CommentCustomRepositoryImpl implements CommentCustomRepository {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Slice<Comment> searchAll(
      UUID reviewId, String direction, Instant after, UUID cursorId, int limit) {
    QComment comment = QComment.comment;

    BooleanExpression condition =
        comment
            .review
            .id
            .eq(reviewId)
            .and(comment.isDeleted.eq(false))
            .and(cursorCondition(comment, direction, after, cursorId));

    List<Comment> result =
        jpaQueryFactory
            .selectFrom(comment)
            .join(comment.user)
            .fetchJoin()
            .where(condition)
            .orderBy(getOrderSpecifiers(direction, comment))
            .limit(limit + 1)
            .fetch();

    boolean hasNext = result.size() > limit;
    if (hasNext) {
      result = result.subList(0, limit);
    }

    return new SliceImpl<>(result, PageRequest.of(0, limit), hasNext);
  }

  private OrderSpecifier<?>[] getOrderSpecifiers(String direction, QComment comment) {
    Order order = "DESC".equalsIgnoreCase(direction) ? Order.DESC : Order.ASC;
    return new OrderSpecifier[] {
      new OrderSpecifier<>(order, comment.createdAt), new OrderSpecifier<>(order, comment.id)
    };
  }

  private BooleanExpression cursorCondition(
      QComment comment, String direction, Instant after, UUID cursorId) {
    if (after == null || cursorId == null) {
      return null;
    }

    if ("DESC".equalsIgnoreCase(direction)) {
      return comment
          .createdAt
          .lt(after)
          .or(comment.createdAt.eq(after).and(comment.id.lt(cursorId)));
    } else { // ASC
      return comment
          .createdAt
          .gt(after)
          .or(comment.createdAt.eq(Instant.from(after)).and(comment.id.gt(cursorId)));
    }
  }
}
