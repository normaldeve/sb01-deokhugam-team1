package com.codeit.duckhu.domain.review.controller;

import com.codeit.duckhu.domain.review.dto.CursorPageResponsePopularReviewDto;
import com.codeit.duckhu.domain.review.dto.CursorPageResponseReviewDto;
import com.codeit.duckhu.domain.review.dto.ReviewCreateRequest;
import com.codeit.duckhu.domain.review.dto.ReviewDto;
import com.codeit.duckhu.domain.review.dto.ReviewLikeDto;
import com.codeit.duckhu.domain.review.dto.ReviewSearchRequestDto;
import com.codeit.duckhu.domain.review.dto.ReviewUpdateRequest;
import com.codeit.duckhu.domain.review.service.ReviewService;
import com.codeit.duckhu.domain.user.entity.User;
import com.codeit.duckhu.domain.user.exception.UserException;
import com.codeit.duckhu.global.exception.ErrorCode;
import com.codeit.duckhu.global.type.Direction;
import com.codeit.duckhu.global.type.PeriodType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  @PostMapping
  public ResponseEntity<ReviewDto> createReview(@Valid @RequestBody ReviewCreateRequest request) {
    log.info("리뷰 생성 요청 : {}", request);
    ReviewDto review = reviewService.createReview(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(review);
  }

  @PostMapping("/{reviewId}/like")
  public ResponseEntity<ReviewLikeDto> likeReview(
      @PathVariable("reviewId") UUID reviewId, HttpServletRequest httpServletRequest) {
    log.info("리뷰 좋아요 요청 : {}", reviewId);
    User authenticatedUser = (User) httpServletRequest.getAttribute("authenticatedUser");
    if (authenticatedUser == null) { // 로그인 하지 않은 사용자가 들어왔을때
      log.warn("비인증 사용자 리뷰 좋아요 요청 차단");
      throw new UserException(ErrorCode.UNAUTHORIZED_USER);
    }
    ReviewLikeDto result = reviewService.likeReview(reviewId, authenticatedUser.getId());
    return ResponseEntity.ok(result);
  }

  @PatchMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> updateReview(
      @PathVariable UUID reviewId,
      @RequestHeader(value = "Deokhugam-Request-User-ID") UUID userId,
      @Valid @RequestBody ReviewUpdateRequest request) {
    log.info("리뷰 수정 요청 : {}", reviewId);
    ReviewDto review = reviewService.updateReview(userId, reviewId, request);
    return ResponseEntity.ok(review);
  }

  @DeleteMapping("/{reviewId}")
  public ResponseEntity<Void> softDeleteReview(
      @PathVariable("reviewId") UUID reviewId,
      @RequestHeader(value = "Deokhugam-Request-User-ID") UUID userId) {
    log.info("리뷰 삭제 요청 : {}", reviewId);
    reviewService.softDeleteReviewById(userId, reviewId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{reviewId}/hard")
  public ResponseEntity<Void> hardDeleteReview(
      @PathVariable("reviewId") UUID reviewId,
      @RequestHeader(value = "Deokhugam-Request-User-ID") UUID userId) {
    log.info("리뷰 하드 삭제 요청 : {}", reviewId);
    reviewService.hardDeleteReviewById(userId, reviewId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> getReviewById(
      HttpServletRequest httpServletRequest, @PathVariable("reviewId") UUID reviewId) {
    log.info("리뷰 상세 조회 요청 : {}", reviewId);
    User authenticatedUser = (User) httpServletRequest.getAttribute("authenticatedUser");
    if (authenticatedUser == null) { // 로그인 하지 않은 사용자가 들어왔을때
      log.warn("비인증 사용자 리뷰 상세 조회 요청 차단");
      throw new UserException(ErrorCode.UNAUTHORIZED_USER);
    }

    ReviewDto review = reviewService.getReviewById(authenticatedUser.getId(), reviewId);
    return ResponseEntity.ok(review);
  }

  @GetMapping
  public ResponseEntity<CursorPageResponseReviewDto> getReviews(
      HttpServletRequest httpServletRequest,
      @RequestParam(name = "keyword", required = false) String keyword,
      @RequestParam(name = "orderBy", required = false) String orderBy,
      @RequestParam(name = "direction", required = false, defaultValue = "DESC")
          Direction direction,
      @RequestParam(name = "userId", required = false) UUID userId,
      @RequestParam(name = "bookId", required = false) UUID bookId,
      @RequestParam(name = "cursor", required = false) String cursor,
      @RequestParam(name = "after", required = false) Instant after,
      @RequestParam(name = "limit", required = false) Integer limit) {
    log.info("리뷰 목록 조회 요청 : {}, {}, {}", keyword, orderBy, direction);

    User authenticatedUser = (User) httpServletRequest.getAttribute("authenticatedUser");
    UUID currentUserId = (authenticatedUser != null) ? authenticatedUser.getId() : null;

    ReviewSearchRequestDto requestDto =
        ReviewSearchRequestDto.builder()
            .keyword(keyword)
            .orderBy(orderBy)
            .direction(direction)
            .userId(userId)
            .bookId(bookId)
            .cursor(cursor)
            .after(after)
            .limit(limit != null ? limit : 50)
            .build();

    CursorPageResponseReviewDto result = reviewService.findReviews(requestDto, currentUserId);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/popular")
  public ResponseEntity<CursorPageResponsePopularReviewDto> getPopularReviews(
      @RequestParam(name = "period") PeriodType period,
      @RequestParam(name = "direction", required = false, defaultValue = "ASC") Direction direction,
      @RequestParam(name = "cursor", required = false) String cursor,
      @RequestParam(name = "after", required = false) Instant after,
      @RequestParam(name = "limit", required = false) Integer limit) {
    log.info("인기 리뷰 목록 조회 요청 : {}, {}", period, direction);

    CursorPageResponsePopularReviewDto result =
        reviewService.getPopularReviews(period, direction, cursor, after, limit);
    return ResponseEntity.ok(result);
  }
}
