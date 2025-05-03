package com.codeit.duckhu.domain.review.repository;

import com.codeit.duckhu.domain.review.entity.Review;
import com.codeit.duckhu.domain.review.repository.custom.ReviewRepositoryCustom;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID>, ReviewRepositoryCustom {

  Optional<Review> findByUserIdAndBookId(UUID userId, UUID bookId);

  /** 해당 도서에 작성된 논리 삭제되지 않은 리뷰 수를 조회합니다. 리뷰가 없을 경우 0을 반환합니다. - 도서 상세 조회 시 reviewCount에 사용됩니다. */
  // @Query("SELECT COUNT(r) FROM Review r WHERE r.book.id = :bookId AND r.isDeleted = false")
  @Query("SELECT COUNT(r) FROM Review r WHERE r.book.id = :bookId AND r.isDeleted = false")
  int countByBookId(@Param("bookId") UUID bookId);

  /** 해당 도서의 논리 삭제되지 않은 리뷰들의 평균 평점을 계산합니다. - 리뷰가 없을 경우 0.0을 반환합니다. - 도서 상세 조회 시 rating에 사용됩니다. */
  //    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.book.id = :bookId AND
  // r.isDeleted = false")
  @Query(
      "SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.book.id = :bookId AND r.isDeleted = false")
  double calculateAverageRatingByBookId(@Param("bookId") UUID bookId);

  // 해당 도서에 작성된 논리 삭제되지 않은 특정 기간 내에 작성된 리뷰 수를 조회합니다.
  @Query(
      "SELECT COUNT(r) FROM Review r WHERE r.book.id = :bookId AND r.createdAt BETWEEN :from AND :to AND r.isDeleted = false")
  int countByBookIdAndCreatedAtBetween(
      @Param("bookId") UUID bookId, @Param("from") Instant from, @Param("to") Instant to);

  // 해당 도서에 작성된 논리 삭제되지 않은 특정 기간 내에 리뷰들의 평균 평점을 조회합니다.
  @Query(
      "SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.book.id = :bookId AND r.createdAt BETWEEN :from AND :to AND r.isDeleted = false")
  double calculateAverageRatingByBookIdAndCreatedAtBetween(
      @Param("bookId") UUID bookId, @Param("from") Instant from, @Param("to") Instant to);

  List<Review> findAllByIsDeletedFalse();
}
