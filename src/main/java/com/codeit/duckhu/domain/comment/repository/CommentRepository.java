package com.codeit.duckhu.domain.comment.repository;

import com.codeit.duckhu.domain.comment.domain.Comment;
import com.codeit.duckhu.domain.review.entity.Review;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentCustomRepository {

  List<Comment> findByReview_Id(UUID reviewId);

  // 삭제되지 않은 댓글만 조회
  List<Comment> findByReview_IdAndIsDeletedFalse(UUID reviewId);

  /**
   * 특정 리뷰에 대해 특정 기간 동안 작성된 (삭제되지 않은) 댓글 수를 계산
   *
   * @param review    리뷰 엔티티
   * @param startTime 시작 시간
   * @param endTime   종료 시간
   * @return 해당 기간 동안의 댓글 수
   */
  int countByReviewAndIsDeletedFalseAndCreatedAtBetween(Review review, Instant startTime, Instant endTime);

  /**
   * 특정 리뷰에 대해 모든 기간 동안 작성된 (삭제되지 않은) 댓글 수를 계산
   * (ALL TIME 기간 계산 시 사용)
   *
   * @param review 리뷰 엔티티
   * @return 전체 댓글 수
   */
  int countByReviewAndIsDeletedFalse(Review review);

  // 특정 리뷰에 대한 댓글 수를 계산
  int countByReviewIdAndIsDeletedFalse(UUID reviewId);
}
