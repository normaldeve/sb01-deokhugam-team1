package com.codeit.duckhu.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.duckhu.domain.book.entity.Book;
import com.codeit.duckhu.domain.book.repository.BookRepository;
import com.codeit.duckhu.domain.comment.repository.CommentRepository;
import com.codeit.duckhu.domain.review.dto.CursorPageResponsePopularReviewDto;
import com.codeit.duckhu.domain.review.dto.CursorPageResponseReviewDto;
import com.codeit.duckhu.domain.review.dto.PopularReviewDto;
import com.codeit.duckhu.domain.review.dto.ReviewCreateRequest;
import com.codeit.duckhu.domain.review.dto.ReviewDto;
import com.codeit.duckhu.domain.review.dto.ReviewSearchRequestDto;
import com.codeit.duckhu.domain.review.dto.ReviewUpdateRequest;
import com.codeit.duckhu.domain.review.entity.PopularReview;
import com.codeit.duckhu.domain.review.entity.Review;
import com.codeit.duckhu.domain.review.mapper.ReviewMapper;
import com.codeit.duckhu.domain.review.repository.PopularReviewRepository;
import com.codeit.duckhu.domain.review.repository.ReviewRepository;
import com.codeit.duckhu.domain.review.service.impl.ReviewServiceImpl;
import com.codeit.duckhu.domain.user.entity.User;
import com.codeit.duckhu.domain.user.repository.UserRepository;
import com.codeit.duckhu.global.exception.DomainException;
import com.codeit.duckhu.global.exception.ErrorCode;
import com.codeit.duckhu.global.type.Direction;
import com.codeit.duckhu.domain.book.storage.ThumbnailImageStorage;
import com.codeit.duckhu.domain.notification.service.NotificationService;
import com.codeit.duckhu.global.type.PeriodType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.swing.text.html.Option;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.mockito.Mockito.doNothing;
import com.codeit.duckhu.domain.review.dto.ReviewLikeDto;

/** 리뷰 서비스 테스트 클래스 TDD 방식으로 구현 예정 */

/**
 * TODO : 실패 케이스 작성 (FORBIDDEN, NOT_FOUND ...)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReviewServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private BookRepository bookRepository;

  @Mock private ReviewRepository reviewRepository;

  @Mock private ReviewMapper reviewMapper;

  @Mock private CommentRepository commentRepository;

  @Mock private Direction direction;
  
  @Mock private ThumbnailImageStorage thumbnailImageStorage;

  @Mock private PopularReviewRepository popularReviewRepository;
  
  @Mock private NotificationService notificationService;

  @InjectMocks private ReviewServiceImpl reviewService;

  private User testUser;
  private Book testBook;
  private Review testReview;
  private ReviewDto testReviewDto;
  private ReviewCreateRequest testCreateRequest;
  private UUID testReviewId;
  private PeriodType type;
  private PopularReview popularReview;
  private PopularReviewDto popularReviewDto;
  private UUID testUserId;
  private UUID testBookId;
  private ReviewUpdateRequest testreviewUpdateRequest;
  private final String TEST_THUMBNAIL_URL = "http://example.com/test.jpg";

  @BeforeEach
  void setUp() {
    // 테스트용 ID 생성
    testReviewId = UUID.randomUUID();
    testUserId = UUID.randomUUID();
    testBookId = UUID.randomUUID();

    // 테스트용 객체 생성 - 스텁 설정 없이 mock 객체만 생성
    testUser = mock(User.class);
    testBook = mock(Book.class);
    testReview = mock(Review.class);

    // 테스트용 DTO 생성
    testReviewDto =
        ReviewDto.builder()
            .id(testReviewId)
            .content("볼만해요")
            .rating(3)
            .commentCount(0)
            .likeCount(0)
            .likedByMe(false)
            .userId(testUserId)
            .bookId(testBookId)
            .userNickname("테스터")
            .bookTitle("테스트 도서")
            .bookThumbnailUrl("http://example.com/test.jpg")
            .createdAt(LocalDateTime.now())
            .build();

    // 테스트용 Create 요청 생성
    testCreateRequest =
        ReviewCreateRequest.builder()
            .userId(testUserId)
            .bookId(testBookId)
            .content("볼만해요")
            .rating(3)
            .build();

    // 테스트용 Update 요청 생성
    testreviewUpdateRequest =
        ReviewUpdateRequest.builder()
            .content("재밌어요")
            .rating(5)
            .userId(testUserId)
            .bookId(testBookId)
            .build();
  }

  @Nested
  @DisplayName("리뷰 생성 테스트")
  class Create {

    @Test
    @DisplayName("리뷰 생성 성공")
    void createReview_shouldCreateReview() {
      // Given
      when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
      when(bookRepository.findById(testBookId)).thenReturn(Optional.of(testBook));
      when(reviewRepository.findByUserIdAndBookId(testUserId, testBookId))
          .thenReturn(Optional.empty());
      when(reviewMapper.toEntity(any(), any(), any())).thenReturn(testReview);
      when(reviewRepository.save(any())).thenReturn(testReview);
      when(testReview.getUser()).thenReturn(testUser);
      when(testUser.getId()).thenReturn(testUserId);
      when(testUser.getNickname()).thenReturn("테스터");
      when(testReview.getBook()).thenReturn(testBook);
      when(testBook.getId()).thenReturn(testBookId);
      when(testBook.getTitle()).thenReturn("테스트 도서");
      when(testBook.getThumbnailUrl()).thenReturn("test.jpg");
      // thumbnailImageStorage.get() 모킹 추가
      when(thumbnailImageStorage.get(any())).thenReturn(TEST_THUMBNAIL_URL);
      // 3개 인자를 받는 toDto 메소드 모킹
      when(reviewMapper.toDto(any(Review.class), anyString(), eq(testUserId))).thenReturn(testReviewDto);

      // When
      ReviewDto result = reviewService.createReview(testCreateRequest);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getRating()).isEqualTo(testReviewDto.getRating());
      assertThat(result.getContent()).isEqualTo(testReviewDto.getContent());
    }

    @Test
    @DisplayName("이미 도서에 리뷰가 존재하는 경우 예외가 발생해야 함")
    void createReview_shouldThrowException_whenReviewAlreadyExists() {
      // Given
      when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
      when(bookRepository.findById(testBookId)).thenReturn(Optional.of(testBook));
      when(reviewRepository.findByUserIdAndBookId(testUserId, testBookId))
          .thenReturn(Optional.of(testReview));

      // When & Then
      assertThrows(DomainException.class, () -> reviewService.createReview(testCreateRequest));
      verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("논리 삭제했을 때 리뷰 생성 성공")
    void createReview_withSoftDelete_shouldReturnNewReview() {
      // Given
      when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
      when(bookRepository.findById(testBookId)).thenReturn(Optional.of(testBook));
      when(reviewRepository.findByUserIdAndBookId(testUserId, testBookId)) .thenReturn(Optional.of(testReview));

      // 기존 리뷰가 논리 삭제 된 상태
      when(testReview.isDeleted()).thenReturn(true);
      when(testReview.getBook()).thenReturn(testBook);

      // 새로운 리뷰 저장
      when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
      when(thumbnailImageStorage.get(any())).thenReturn(TEST_THUMBNAIL_URL);
      when(reviewMapper.toDto(any(Review.class), anyString(), eq(testUserId))).thenReturn(testReviewDto);

      // When
      ReviewDto result = reviewService.createReview(testCreateRequest);

      // Then
      assertThat(result).isNotNull();
      verify(testReview).updateContent(testCreateRequest.getContent());
      verify(testReview).updateRating(testCreateRequest.getRating());
      verify(reviewRepository).save(testReview);
    }
  }

  @Nested
  @DisplayName("리뷰 검색 테스트")
  class GetReviewById{
    @Test
    @DisplayName("ID로 리뷰 조회 테스트")
    void getReviewById_shouldReturnReview() {
      // Given
      when(reviewRepository.findById(testReviewId)).thenReturn(Optional.of(testReview));
      when(testReview.getUser()).thenReturn(testUser);
      when(testUser.getId()).thenReturn(testUserId);
      when(testUser.getNickname()).thenReturn("테스터");
      when(testReview.getBook()).thenReturn(testBook);
      when(testBook.getId()).thenReturn(testBookId);
      when(testBook.getTitle()).thenReturn("테스트 도서");
      when(testBook.getThumbnailUrl()).thenReturn("test.jpg");
      when(thumbnailImageStorage.get(any())).thenReturn(TEST_THUMBNAIL_URL);
      when(reviewMapper.toDto(eq(testReview), anyString(), eq(testUserId))).thenReturn(testReviewDto);

      // When
      ReviewDto result = reviewService.getReviewById(testUserId, testReviewId);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).isEqualTo(testReviewDto.getContent());
      assertThat(result.getRating()).isEqualTo(testReviewDto.getRating());
    }

    @Test
    @DisplayName("존재하지 않는 리뷰 조회")
    void getReviewById_shouldThrowException() {
      // Given
      when(reviewRepository.findById(testReviewId)).thenReturn(Optional.empty());

      // When & Then
      DomainException exception = assertThrows(DomainException.class,
          () -> reviewService.getReviewById(testUserId, testReviewId));
      assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
      verify(reviewRepository).findById(testReviewId);
    }
  }

  @Nested
  @DisplayName("리뷰 삭제 테스트")
  class delete {

    @Test
    @DisplayName("ID로 리뷰 하드 삭제 테스트")
    void hardDeleteReviewById_shouldReturnSuccess() {
      // Given: findById 리턴과 delete 설정
      doReturn(Optional.of(testReview)).when(reviewRepository).findById(testReviewId);
      when(testReview.getUser()).thenReturn(testUser); // User 객체를 반환하도록 설정
      when(testUser.getId()).thenReturn(testUserId);   // User ID를 반환하도록 설정
      when(testReview.getBook()).thenReturn(testBook); // Book 객체를 반환하도록 설정
      willDoNothing().given(reviewRepository).delete(testReview);

      // When : 서비스 호출 시 예외가 나지 않아야 하고
      assertDoesNotThrow(() -> reviewService.hardDeleteReviewById(testUserId, testReviewId));

      // Then: repository.findById + repository.delete 가 호출됐는지 검증, 불필요한 추가 호출이 없는지도 검증
      verify(reviewRepository).findById(testReviewId);
      verify(reviewRepository).delete(testReview);
    }

    @Test
    @DisplayName("다른 유저가 하드 삭제시 에러")
    void hardDeleteReviewById_shouldThrowException() {
      // Given
      when(reviewRepository.findById(testReviewId)).thenReturn(Optional.of(testReview));
      when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
      when(testReview.getUser()).thenReturn(testUser);
      when(testUser.getId()).thenReturn(UUID.randomUUID()); // 다른 유저 ID 설정

      // When & Then
      DomainException exception = assertThrows(DomainException.class,
          () -> reviewService.hardDeleteReviewById(testUserId, testReviewId));
      assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NO_AUTHORITY_USER);
    }

    @Test
    @DisplayName("ID로 리뷰 소프트 삭제 테스트")
    void softDeleteReviewById_shouldReturnSuccess() {
      // Given
      doReturn(Optional.of(testReview)).when(reviewRepository).findById(testReviewId);
      when(testReview.getUser()).thenReturn(testUser);
      when(testUser.getId()).thenReturn(testUserId);
      when(testReview.getBook()).thenReturn(testBook); // Book 객체를 반환하도록 설정

      // When
      reviewService.softDeleteReviewById(testUserId, testReviewId);

      // Then
      verify(testReview).softDelete();
      verify(reviewRepository).save(testReview);
    }

    @Test
    @DisplayName("다른 유저가 소프트 삭제시 에러")
    void softDeleteReviewById_shouldThrowException() {
      // Given
      when(reviewRepository.findById(testReviewId)).thenReturn(Optional.of(testReview));
      when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
      when(testReview.getUser()).thenReturn(testUser);
      when(testUser.getId()).thenReturn(UUID.randomUUID()); // 다른 유저

      // When & Then
      DomainException exception = assertThrows(DomainException.class,
          () -> reviewService.softDeleteReviewById(testUserId, testReviewId));
      assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NO_AUTHORITY_USER);
    }
  }

  @Nested
  @DisplayName("리뷰 업데이트 테스트")
  class update {
    @Test
    @DisplayName("리뷰 업데이트 성공")
    void updateReview_shouldReturnUpdateReview() {
      // Given
      when(reviewRepository.findById(testReviewId)).thenReturn(Optional.of(testReview));
      when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
      when(reviewRepository.save(testReview)).thenReturn(testReview);
      when(testReview.getUser()).thenReturn(testUser);
      when(testUser.getId()).thenReturn(testUserId);
      when(testUser.getNickname()).thenReturn("테스터");
      when(testReview.getBook()).thenReturn(testBook);
      when(testBook.getId()).thenReturn(testBookId);
      when(testBook.getTitle()).thenReturn("테스트 도서");
      when(testBook.getThumbnailUrl()).thenReturn("test.jpg");
      when(thumbnailImageStorage.get(any())).thenReturn(TEST_THUMBNAIL_URL);
      when(reviewMapper.toDto(eq(testReview), anyString(), eq(testUserId))).thenReturn(testReviewDto);

      // When
      ReviewDto result = reviewService.updateReview(testUserId, testReviewId, testreviewUpdateRequest);

      // Then
      assertThat(result).isNotNull();
      assertThat(result).isEqualTo(testReviewDto);
    }

    @Test
    @DisplayName("다른 유저가 업데이트시 에러")
    void updateReview_shouldThrowException() {
      // Given
      User loginUser = mock(User.class);
      User reviewOwner = mock(User.class);

      when(reviewRepository.findById(testReviewId)).thenReturn(Optional.of(testReview));
      when(userRepository.findById(testUserId)).thenReturn(Optional.of(loginUser));
      when(testReview.getUser()).thenReturn(reviewOwner);
      when(loginUser.getId()).thenReturn(UUID.randomUUID());
      when(reviewOwner.getId()).thenReturn(UUID.randomUUID());

      when(testReview.getBook()).thenReturn(testBook);
      when(testBook.getId()).thenReturn(testBookId);
      when(testBook.getTitle()).thenReturn("테스트 도서");
      when(testBook.getThumbnailUrl()).thenReturn("test.jpg");
      when(thumbnailImageStorage.get(any())).thenReturn(TEST_THUMBNAIL_URL);
      when(reviewMapper.toDto(eq(testReview), anyString(), eq(testUserId))).thenReturn(testReviewDto);

      // When & Then
      DomainException exception = assertThrows(DomainException.class,
          () -> reviewService.updateReview(testUserId, testReviewId, testreviewUpdateRequest));
      assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NO_AUTHORITY_USER);
    }

    @Test
    @DisplayName("논리 삭제된 리뷰 업데이트시 에러")
    void updateReview_shouldThrowException_whenIsDeleted() {
      // Given
      when(reviewRepository.findById(testReviewId)).thenReturn(Optional.of(testReview));
      when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
      when(testReview.getUser()).thenReturn(testUser);
      when(testUser.getId()).thenReturn(testUserId);
      when(testReview.isDeleted()).thenReturn(true); // 리뷰가 논리 삭제된 상태

      // When & Then
      DomainException exception = assertThrows(DomainException.class,
          () -> reviewService.updateReview(testUserId, testReviewId, testreviewUpdateRequest));
      assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("리뷰 좋아요 테스트")
  class LikeReview {

    @Test
    @DisplayName("좋아요가 없는 상태에서 좋아요 누르면 likeCount 증가 및 liked=true 반환")
    void likeReview_firstTime_likeCountIncreased() {
      // Given
      when(reviewRepository.findById(testReviewId)).thenReturn(Optional.of(testReview));
      when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
      when(userRepository.existsById(testUserId)).thenReturn(true); // 존재하는 userId 설정
      when(testReview.liked(testUserId)).thenReturn(false).thenReturn(true); // 첫 호출에서 false, 두 번째 호출에서 true 반환
      when(reviewRepository.save(testReview)).thenReturn(testReview);
      when(testReview.getBook()).thenReturn(testBook);
      when(testReview.getId()).thenReturn(testReviewId);

      // When
      ReviewLikeDto result = reviewService.likeReview(testReviewId, testUserId);

      // Then
      verify(testReview).increaseLikeCount(testUserId);
      assertThat(result.isLiked()).isTrue();
      assertThat(result.getReviewId()).isEqualTo(testReviewId);
      assertThat(result.getUserId()).isEqualTo(testUserId);
    }

    @Test
    @DisplayName("좋아요가 된 상태에서 다시 누르면 언라이크 처리, liked=false 반환")
    void likeReview_toggleOff_likeCountDecreased() {
      // Given
      when(reviewRepository.findById(testReviewId)).thenReturn(Optional.of(testReview));
      when(userRepository.existsById(testUserId)).thenReturn(true); // 존재하는 userId 설정
      when(testReview.liked(testUserId)).thenReturn(true).thenReturn(false); // 첫 호출에서 true, 두 번째 호출에서 false 반환
      when(testReview.getId()).thenReturn(testReviewId);
      when(testReview.getUser()).thenReturn(testUser);

      // When
      ReviewLikeDto result = reviewService.likeReview(testReviewId, testUserId);

      // Then
      verify(testReview).decreaseLikeCount(testUserId);
      assertThat(result.isLiked()).isFalse();
      assertThat(result.getReviewId()).isEqualTo(testReviewId);
      assertThat(result.getUserId()).isEqualTo(testUserId);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰에 좋아요 요청 시 예외")
    void likeReview_reviewNotFound() {
      // Given
      when(reviewRepository.findById(testReviewId)).thenReturn(Optional.empty());

      // When & Then
      DomainException exception = assertThrows(DomainException.class,
          () -> reviewService.likeReview(testReviewId, testUserId));
      assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 유저가 좋아요 요청 시 예외")
    void likeReview_userNotFount() {
      // Given
      when(userRepository.findById(testUserId)).thenReturn(Optional.empty());
      when(reviewRepository.findById(testReviewId)).thenReturn(Optional.of(testReview));

      // When & Then
      DomainException exception = assertThrows(DomainException.class,
          () -> reviewService.likeReview(testReviewId, testUserId));
      assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("리뷰 커서 페이지네이션 테스트")
  class FindReviews {

    @Test
    @DisplayName("리뷰 커서 페이지네이션 테스트 성공")
    void findReviews_success() {
      // Given
      List<Review> reviewList = new ArrayList<>();
      UUID currentUserIdForTest = null; // 테스트용 현재 사용자 ID (null 또는 testUserId 등)

      // 정확한 파라미터로 stubbing 설정
      when(reviewRepository.findReviewsWithCursor(
          eq(null), eq("createdAt"), eq(Direction.DESC), // 기본값 가정
          eq(null), eq(null), eq(null), eq(null), eq(51))) // limit+1
          .thenReturn(reviewList);
      when(thumbnailImageStorage.get(any())).thenReturn(TEST_THUMBNAIL_URL);
      when(reviewMapper.toDto(any(Review.class), anyString(), eq(currentUserIdForTest))).thenReturn(
          testReviewDto); // 예시 DTO 반환

      // When
      ReviewSearchRequestDto requestDto = ReviewSearchRequestDto.builder().build(); // 기본값 사용
      // 수정: findReviews 호출 시 currentUserId 전달
      CursorPageResponseReviewDto result = reviewService.findReviews(requestDto,
          currentUserIdForTest);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.isHasNext()).isFalse(); // reviewList가 비어있으므로 hasNext는 false 예상
      assertThat(result.getContent()).isEmpty(); // reviewList가 비어있으므로 content는 비어있음 예상
    }
  }

  @Nested
  @DisplayName("인기 리뷰 커서 페이지네이션 테스트")
  class findPopularReviews {

    @Test
    @DisplayName("인기 리뷰 커서 페이지네이션 테스트 성공")
    void findPopularReviews_success() {
      // Given
      List<PopularReview> popularReviews = new ArrayList<>();
      popularReview = mock(PopularReview.class);

      when(popularReviewRepository.findReviewsWithCursor(
          eq(null), eq(Direction.DESC), eq(null), eq(null), eq(51)))
          .thenReturn(popularReviews);
      when(thumbnailImageStorage.get(any())).thenReturn(TEST_THUMBNAIL_URL);
      when(reviewMapper.toDto(any(Review.class), anyString(), eq(testUserId)))
          .thenReturn(testReviewDto);

      // When
      ReviewSearchRequestDto requestDto = ReviewSearchRequestDto.builder().build();
      CursorPageResponsePopularReviewDto result = reviewService.getPopularReviews(
          PeriodType.DAILY, Direction.DESC, null, null, 50);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.isHasNext()).isFalse();
      assertThat(result.getContent()).isEmpty();
    }
  }
}
