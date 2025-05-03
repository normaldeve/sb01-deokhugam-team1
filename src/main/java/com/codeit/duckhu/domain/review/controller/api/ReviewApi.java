package com.codeit.duckhu.domain.review.controller.api;

import com.codeit.duckhu.domain.review.dto.ReviewCreateRequest;
import com.codeit.duckhu.domain.review.dto.ReviewDto;
import com.codeit.duckhu.domain.review.dto.ReviewUpdateRequest;
import com.codeit.duckhu.global.type.Direction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "리뷰 관리", description = "리뷰 관련 API")
public interface ReviewApi {

  @Operation(summary = "리뷰 등록", description = "새로운 리뷰를 등록합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "리뷰 등록 성공",
          content = @Content(schema = @Schema(implementation = ReviewDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "잘못된 요청(입력값 검증 실패)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "404",
          description = "도서 정보 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "409",
          description = "이미 작성된 리뷰 존재",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "500",
          description = "서버 내부 오류",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping(value = "/api/reviews")
  ResponseEntity<ReviewDto> createReview(@RequestBody ReviewCreateRequest request);

  @Operation(summary = "리뷰 수정", description = "본인이 작성한 리뷰를 수정합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "리뷰 수정 성공",
          content = @Content(schema = @Schema(implementation = ReviewDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "잘못된 요청(입력값 검증 실패)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "403",
          description = "리뷰 수정 권한 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "404",
          description = "리뷰 정보 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "500",
          description = "서버 내부 오류",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PatchMapping(value = "/api/reviews/{reviewId}")
  ResponseEntity<ReviewDto> updateReview(
      @PathVariable("reviewId") UUID reviewId, @RequestBody ReviewUpdateRequest request);

  @Operation(summary = "리뷰 논리 삭제", description = "본인이 작성한 리뷰를 논리적으로 삭제합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "리뷰 삭제 성공",
        content = @Content),
      @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청(요청자 ID 누락)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "리뷰 삭제 권한 없음",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "리뷰 정보 없음",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @DeleteMapping(value = "/api/reviews/{reviewId}")
  ResponseEntity<Void> deleteReview(
      @PathVariable("reviewId") UUID reviewId,
      @RequestHeader(value = "Deokhugam-Request-User-ID") UUID userId);

  @Operation(summary = "리뷰 물리 삭제", description = "본인이 작성한 리뷰를 물리적으로 삭제합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "리뷰 삭제 성공",
          content = @Content),
      @ApiResponse(
          responseCode = "400",
          description = "잘못된 요청(요청자 ID 누락)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "403",
          description = "리뷰 삭제 권한 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "404",
          description = "리뷰 정보 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "500",
          description = "서버 내부 오류",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @DeleteMapping(value = "/api/reviews/{reviewId}/hard")
  ResponseEntity<Void> hardDeleteReview(
      @PathVariable("reviewId") UUID reviewId,
      @RequestHeader(value = "Deokhugam-Request-User-ID") UUID userId);

  @Operation(summary = "리뷰 좋아요", description = "리뷰에 좋아요를 추가하거나 취소합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "리뷰 좋아요 성공",
          content = @Content(schema = @Schema(implementation = ReviewDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "잘못된 요청(요청자 ID 누락)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "404",
          description = "리뷰 정보 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "500",
          description = "서버 내부 오류",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping(value = "/api/reviews/{reviewId}/like")
  ResponseEntity<ReviewDto> likeReview(
      @PathVariable("reviewId") UUID reviewId,
      @RequestHeader(value = "Deokhugam-Request-User-ID") UUID userId);

  @Operation(summary = "리뷰 상세 정보 조회", description = "리뷰 ID로 상세 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "리뷰 상세 정보 조회 성공",
          content = @Content(schema = @Schema(implementation = ReviewDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "잘못된 요청(요청자 ID 누락)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "404",
          description = "리뷰 정보 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "500",
          description = "서버 내부 오류",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping(value = "/api/reviews/{reviewId}")
  ResponseEntity<ReviewDto> getReviewById(
      @PathVariable("reviewId") UUID reviewId);

  @Operation(summary = "리뷰 목록 조회", description = "검색 조건에 맞는 리뷰 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "리뷰 목록 조회 성공",
          content = @Content(schema = @Schema(implementation = List.class))),
      @ApiResponse(
          responseCode = "400",
          description = "잘못된 요청(정렬 기준 오류, 페이지네이션 파라미터 오류, 요청자 ID 누락)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "500",
          description = "서버 내부 오류",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping(value = "/api/reviews")
  ResponseEntity<List<ReviewDto>> getReviews(
      @RequestParam(name = "keyword", required = false) String keyword,
      @RequestParam(name = "orderBy", required = false) String orderBy,
      @RequestParam(name = "direction", required = false, defaultValue = "DESC")
      Direction direction,
      @RequestParam(name = "userId", required = false) UUID userId,
      @RequestParam(name = "bookId", required = false) UUID bookId,
      @RequestParam(name = "cursor", required = false) String cursor,
      @RequestParam(name = "after", required = false) Instant after,
      @RequestParam(name = "limit", required = false) Integer limit);

  @Operation(summary = "인기 리뷰 목록 조회", description = "기간별 인기 리뷰 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "인기 리뷰 목록 조회 성공",
          content = @Content(schema = @Schema(implementation = List.class))),
      @ApiResponse(
          responseCode = "400",
          description = "잘못된 요청(랭킹 기간 오류, 정렬 방향 오류 등)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(
          responseCode = "500",
          description = "서버 내부 오류",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping(value = "/api/reviews/popular")
  ResponseEntity<List<ReviewDto>> getPopularReviews(
      @RequestParam(name = "period", required = false) String period,
      @RequestParam(name = "direction", required = false, defaultValue = "DESC")
      Direction direction,
      @RequestParam(name = "cursor", required = false) String cursor,
      @RequestParam(name = "after", required = false) Instant after,
      @RequestParam(name = "limit", required = false) Integer limit);
}


