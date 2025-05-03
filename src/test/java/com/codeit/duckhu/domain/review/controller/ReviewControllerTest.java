package com.codeit.duckhu.domain.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.duckhu.domain.review.dto.CursorPageResponsePopularReviewDto;
import com.codeit.duckhu.domain.review.dto.CursorPageResponseReviewDto;
import com.codeit.duckhu.domain.review.dto.PopularReviewDto;
import com.codeit.duckhu.domain.review.dto.ReviewCreateRequest;
import com.codeit.duckhu.domain.review.dto.ReviewDto;
import com.codeit.duckhu.domain.review.dto.ReviewLikeDto;
import com.codeit.duckhu.domain.review.dto.ReviewUpdateRequest;
import com.codeit.duckhu.domain.review.service.ReviewService;
import com.codeit.duckhu.domain.user.entity.User;
import com.codeit.duckhu.global.exception.DomainException;
import com.codeit.duckhu.global.exception.ErrorCode;
import com.codeit.duckhu.global.exception.GlobalExceptionHandler;
import com.codeit.duckhu.global.type.Direction;
import com.codeit.duckhu.global.type.PeriodType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReviewControllerTest {

  private MockMvc mockMvc;

  private ObjectMapper objectMapper;

  @Mock private ReviewService reviewService;

  @Mock private User mockUser;

  private ReviewController reviewController;

  private UUID reviewId;
  private UUID userId;
  private UUID bookId;
  private ReviewDto reviewDto;
  private ReviewCreateRequest createRequest;
  private ReviewUpdateRequest updateRequest;
  private ReviewLikeDto reviewLikeDto;

  @BeforeEach
  void setUp() {
    // 실제 컨트롤러 생성
    reviewController = new ReviewController(reviewService);

    // MockMvc 설정 - GlobalExceptionHandler 추가
    mockMvc =
        MockMvcBuilders.standaloneSetup(reviewController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .addFilter(new CharacterEncodingFilter("UTF-8", true))
            .build();

    objectMapper = new ObjectMapper();

    reviewId = UUID.randomUUID();
    userId = UUID.randomUUID();
    bookId = UUID.randomUUID();

    // Mock User 설정
    when(mockUser.getId()).thenReturn(userId);

    reviewDto =
        ReviewDto.builder()
            .id(reviewId)
            .userId(userId)
            .bookId(bookId)
            .rating(4)
            .content("좋은 책이에요")
            .userNickname("테스터")
            .bookTitle("테스트 도서")
            .bookThumbnailUrl("http://example.com/test.jpg")
            .likeCount(5)
            .commentCount(3)
            .likedByMe(false)
            .createdAt(LocalDateTime.now())
            .build();

    createRequest =
        ReviewCreateRequest.builder()
            .userId(userId)
            .bookId(bookId)
            .rating(4)
            .content("좋은 책이에요")
            .build();

    updateRequest =
        ReviewUpdateRequest.builder()
            .userId(userId)
            .bookId(bookId)
            .rating(5)
            .content("정말 좋은 책이에요!")
            .build();

    reviewLikeDto = ReviewLikeDto.builder().reviewId(reviewId).userId(userId).liked(true).build();
  }

  // 인증된 사용자 정보를 요청에 추가하는 헬퍼 메소드
  private RequestPostProcessor withAuthenticatedUser() {
    return request -> {
      request.setAttribute("authenticatedUser", mockUser);
      return request;
    };
  }

  @Test
  @DisplayName("리뷰 목록 조회 - 커서 페이지네이션")
  void findReviews_Success() throws Exception {
    // Given
    CursorPageResponseReviewDto responseDto =
        CursorPageResponseReviewDto.builder()
            .content(List.of(reviewDto))
            .nextCursor("next-cursor")
            .nextAfter(Instant.now())
            .size(1)
            .hasNext(true)
            .build();

    // 서비스 메소드 모킹 - 컨트롤러 메소드에서 필요한 경우
    when(reviewService.findReviews(any(), any())).thenReturn(responseDto);

    // When & Then - 필요한 응답 코드만 검증
    mockMvc
        .perform(
            get("/api/reviews")
                .param("keyword", "키워드")
                .param("orderBy", "createdAt")
                .param("direction", "DESC")
                .param("userId", userId.toString())
                .param("bookId", bookId.toString())
                .param("limit", "10")
                .header("Deokhugam-Request-User-Id", userId.toString())
                .with(withAuthenticatedUser()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.size").exists());
  }

  @Test
  @DisplayName("리뷰 생성")
  void createReview_Success() throws Exception {
    // Given
    when(reviewService.createReview(any(ReviewCreateRequest.class))).thenReturn(reviewDto);

    // When & Then
    mockMvc
        .perform(
            post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
        .andDo(print())
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("ID로 리뷰 조회")
  void getReviewById_Success() throws Exception {
    // 서비스 메소드 모킹
    when(reviewService.getReviewById(eq(userId), eq(reviewId))).thenReturn(reviewDto);

    // 요청에 인증 사용자 정보 추가
    mockMvc
        .perform(get("/api/reviews/{reviewId}", reviewId).with(withAuthenticatedUser()))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("리뷰 업데이트")
  void updateReview_Success() throws Exception {
    // Given
    when(reviewService.updateReview(eq(userId), eq(reviewId), any(ReviewUpdateRequest.class)))
        .thenReturn(reviewDto);

    // When & Then
    mockMvc
        .perform(
            patch("/api/reviews/{reviewId}", reviewId)
                .header("Deokhugam-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("리뷰 소프트 삭제")
  void softDeleteReview_Success() throws Exception {
    // Given
    doNothing().when(reviewService).softDeleteReviewById(eq(userId), eq(reviewId));

    // When & Then
    mockMvc
        .perform(
            delete("/api/reviews/{reviewId}", reviewId)
                .header("Deokhugam-Request-User-ID", userId.toString()))
        .andDo(print())
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("리뷰 하드 삭제")
  void hardDeleteReview_Success() throws Exception {
    // Given
    doNothing().when(reviewService).hardDeleteReviewById(eq(userId), eq(reviewId));

    // When & Then
    mockMvc
        .perform(
            delete("/api/reviews/{reviewId}/hard", reviewId)
                .header("Deokhugam-Request-User-ID", userId.toString()))
        .andDo(print())
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("인기 리뷰 조회 테스트")
  void getPopularReviews_Success() throws Exception {
    // Given
    PopularReviewDto popularReviewDto =
        PopularReviewDto.builder()
            .id(reviewId)
            .userId(userId)
            .bookId(bookId)
            .userNickname("인기유저")
            .bookTitle("인기도서")
            .bookThumbnailUrl("http://example.com/popular.jpg")
            .likeCount(100)
            .commentCount(20)
            .rank(1)
            .build();

    CursorPageResponsePopularReviewDto responseDto =
        CursorPageResponsePopularReviewDto.builder()
            .content(List.of(popularReviewDto))
            .nextCursor("next-cursor")
            .nextAfter(Instant.now())
            .size(1)
            .hasNext(true)
            .build();

    when(reviewService.getPopularReviews(
            any(PeriodType.class), any(Direction.class), any(), any(), any()))
        .thenReturn(responseDto);

    // When & Then
    mockMvc
        .perform(
            get("/api/reviews/popular")
                .param("period", "WEEKLY")
                .param("direction", "DESC")
                .param("limit", "10"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.size").exists())
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].rank").value(1))
        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].likeCount").value(100));
  }

  @Test
  @DisplayName("리뷰 좋아요 테스트")
  void likeReview_Success() throws Exception {
    // Given
    when(reviewService.likeReview(eq(reviewId), eq(userId))).thenReturn(reviewLikeDto);

    // When & Then
    mockMvc
        .perform(post("/api/reviews/{reviewId}/like", reviewId).with(withAuthenticatedUser()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.reviewId").value(reviewId.toString()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.liked").value(true));
  }

  @Test
  @DisplayName("인증되지 않은 사용자의 리뷰 좋아요 시도 - 401 오류")
  void likeReview_Unauthorized() throws Exception {
    // Given - controller 에서 인증되지 않은 사용자 처리 로직 (MockMvc에 예외 처리기 추가)

    // When & Then
    mockMvc
        .perform(post("/api/reviews/{reviewId}/like", reviewId))
        .andDo(print())
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("리뷰 조회 시 인증되지 않은 사용자 - 401 오류")
  void getReviewById_Unauthorized() throws Exception {
    // Given - controller 에서 인증되지 않은 사용자 처리 로직 (MockMvc에 예외 처리기 추가)

    // When & Then
    mockMvc
        .perform(get("/api/reviews/{reviewId}", reviewId))
        .andDo(print())
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("존재하지 않는 리뷰 업데이트 시도 - 404 오류")
  void updateReview_NotFound() throws Exception {
    // Given
    when(reviewService.updateReview(eq(userId), eq(reviewId), any(ReviewUpdateRequest.class)))
        .thenThrow(new DomainException(ErrorCode.REVIEW_NOT_FOUND));

    // When & Then
    mockMvc
        .perform(
            patch("/api/reviews/{reviewId}", reviewId)
                .header("Deokhugam-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("다른 사용자의 리뷰 업데이트 시도 - 403 오류")
  void updateReview_Forbidden() throws Exception {
    // Given
    UUID otherUserId = UUID.randomUUID();
    when(reviewService.updateReview(eq(otherUserId), eq(reviewId), any(ReviewUpdateRequest.class)))
        .thenThrow(new DomainException(ErrorCode.NO_AUTHORITY_USER));

    // When & Then
    mockMvc
        .perform(
            patch("/api/reviews/{reviewId}", reviewId)
                .header("Deokhugam-Request-User-ID", otherUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andDo(print())
        .andExpect(status().isForbidden());
  }
}
