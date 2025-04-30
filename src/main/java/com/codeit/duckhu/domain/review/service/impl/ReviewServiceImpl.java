package com.codeit.duckhu.domain.review.service.impl;

import com.codeit.duckhu.domain.book.entity.Book;
import com.codeit.duckhu.domain.book.repository.BookRepository;
import com.codeit.duckhu.domain.book.storage.ThumbnailImageStorage;
import com.codeit.duckhu.domain.comment.repository.CommentRepository;
import com.codeit.duckhu.domain.notification.service.NotificationService;
import com.codeit.duckhu.domain.review.dto.CursorPageResponsePopularReviewDto;
import com.codeit.duckhu.domain.review.dto.CursorPageResponseReviewDto;
import com.codeit.duckhu.domain.review.dto.PopularReviewDto;
import com.codeit.duckhu.domain.review.dto.ReviewCreateRequest;
import com.codeit.duckhu.domain.review.dto.ReviewDto;
import com.codeit.duckhu.domain.review.dto.ReviewLikeDto;
import com.codeit.duckhu.domain.review.dto.ReviewSearchRequestDto;
import com.codeit.duckhu.domain.review.dto.ReviewUpdateRequest;
import com.codeit.duckhu.domain.review.entity.PopularReview;
import com.codeit.duckhu.domain.review.entity.Review;
import com.codeit.duckhu.domain.review.mapper.ReviewMapper;
import com.codeit.duckhu.domain.review.repository.PopularReviewRepository;
import com.codeit.duckhu.domain.review.repository.ReviewRepository;
import com.codeit.duckhu.domain.review.service.ReviewService;
import com.codeit.duckhu.domain.user.entity.User;
import com.codeit.duckhu.domain.user.repository.UserRepository;
import com.codeit.duckhu.global.exception.DomainException;
import com.codeit.duckhu.global.exception.ErrorCode;
import com.codeit.duckhu.global.type.Direction;
import com.codeit.duckhu.global.type.PeriodType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 리뷰 서비스 구현체 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;
  private final ReviewMapper reviewMapper;
  private final BookRepository bookRepository;
  private final UserRepository userRepository;
  private final PopularReviewRepository popularRepository;
  // 알림 생성을 위해 DI추가
  private final NotificationService notificationService;

  private final ThumbnailImageStorage thumbnailImageStorage;
  
  // 코멘트 수 업데이트를 위해 CommentRepository 추가
  private final CommentRepository commentRepository;

  @Override
  @Transactional
  public ReviewDto createReview(ReviewCreateRequest request) {
    log.info("새로운 리뷰 생성, rating: {}", request.getRating());

    // 사용자 찾기
    User user =
            userRepository
                    .findById(request.getUserId())
                    .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND));

    // 도서 찾기
    Book book =
            bookRepository
                    .findById(request.getBookId())
                    .orElseThrow(() -> new DomainException(ErrorCode.BOOK_NOT_FOUND));

    // 동일한 도서에 대한 리뷰가 이미 존재하는지 확인
    Optional<Review> existingReview = reviewRepository.findByUserIdAndBookId(request.getUserId(), request.getBookId());
    if (existingReview.isPresent()) {
      if (!existingReview.get().isDeleted()) {
        throw new DomainException(ErrorCode.REVIEW_ALREADY_EXISTS);

      }
      // 삭제된 리뷰인 경우 재활용
      Review review = existingReview.get();
      review.updateContent(request.getContent());
      review.updateRating(request.getRating());
      review.restore(); // 삭제 상태 해제
      reviewRepository.save(review);
      
      // 도서 통계 재계산
      recalculateBookStats(review.getBook());
      
      // 썸네일 이미지를 S3 주소로 가져옵니다.
      String thumbnailUrl = thumbnailImageStorage.get(review.getBook().getThumbnailUrl());
      
      // DTO로 변환하여 반환
      return reviewMapper.toDto(review, thumbnailUrl, request.getUserId());
    }

    // 매퍼를 사용하여 엔티티 생성
    Review review = reviewMapper.toEntity(request, user, book);
    
    // 리뷰 저장
    review = reviewRepository.save(review);

    // jw
    recalculateBookStats(book);

    // jw - 썸네일 이미지를 S3 주소로 가져옵니다.
    String thumbnailUrl = thumbnailImageStorage.get(review.getBook().getThumbnailUrl());

    // DTO로 변환하여 반환
    return reviewMapper.toDto(review, thumbnailUrl, request.getUserId());
  }

  @Override
  public ReviewDto getReviewById(UUID userId, UUID reviewId) {
    log.info("리뷰 조회, ID: {}", reviewId);

    // 리뷰 조회
    Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new DomainException(ErrorCode.REVIEW_NOT_FOUND));

    if (review.isDeleted()) {
      throw new DomainException(ErrorCode.REVIEW_NOT_FOUND);
    }

    // 코멘트 수를 DB에서 직접 가져옵니다
    int commentCount = commentRepository.countByReviewIdAndIsDeletedFalse(reviewId);
    // 리뷰 엔티티에 코멘트 수 설정
    review.updateCommentCount(commentCount);

    // jw - 썸네일 이미지를 S3 주소로 가져옵니다.
    String thumbnailUrl = thumbnailImageStorage.get(review.getBook().getThumbnailUrl());

    // DTO로 변환하여 반환
    return reviewMapper.toDto(review, thumbnailUrl, userId);
  }

  @Transactional
  @Override
  public void hardDeleteReviewById(UUID userId, UUID reviewId) {
    Review review =
        reviewRepository
            .findById(reviewId)
            .orElseThrow(() -> new DomainException(ErrorCode.REVIEW_NOT_FOUND));

    // 사용자가 권한이 있는지 확인
    if (review.getUser().getId().equals(userId)) {
      reviewRepository.delete(review);
    } else {
      throw new DomainException(ErrorCode.NO_AUTHORITY_USER);
    }

    // jw
    recalculateBookStats(review.getBook());
  }

  @Transactional
  @Override
  public void softDeleteReviewById(UUID userId, UUID reviewId) {
    Review review =
        reviewRepository
            .findById(reviewId)
            .orElseThrow(() -> new DomainException(ErrorCode.REVIEW_NOT_FOUND));

    // 사용자가 권한이 있는지 확인
    if (!review.getUser().getId().equals(userId)) {
      throw new DomainException(ErrorCode.NO_AUTHORITY_USER);
    }
    review.softDelete();
    reviewRepository.save(review);

    // jw
    recalculateBookStats(review.getBook());
  }

  @Transactional
  @Override
  public ReviewDto updateReview(UUID userId, UUID reviewId, ReviewUpdateRequest request) {
    Review review =
        reviewRepository
            .findById(reviewId)
            .orElseThrow(() -> new DomainException(ErrorCode.REVIEW_NOT_FOUND));

    // 사용자 찾기
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND));

    if (review.isDeleted()) {
      throw new DomainException(ErrorCode.REVIEW_NOT_FOUND);
    }

    // 사용자가 권한이 있는지 확인
    if (!user.getId().equals(review.getUser().getId())) {
      throw new DomainException(ErrorCode.NO_AUTHORITY_USER);
    }

    review.updateContent(request.getContent());
    review.updateRating(request.getRating());

    Review updatedReview = reviewRepository.save(review);
    log.info("리뷰 업데이트 성공, ID: {}", updatedReview.getId());

    // jw
    recalculateBookStats(updatedReview.getBook());

    // jw - 썸네일을 S3 저장소에서 가져옵니다.
    String thumbnailUrl = thumbnailImageStorage.get(updatedReview.getBook().getThumbnailUrl());

    return reviewMapper.toDto(updatedReview, thumbnailUrl, userId);
  }

  @Transactional
  @Override
  public ReviewLikeDto likeReview(UUID reviewId, UUID userId) {
    Review review =
            reviewRepository
                    .findById(reviewId)
                    .orElseThrow(() -> new DomainException(ErrorCode.REVIEW_NOT_FOUND));

    if (review.isDeleted()) {
      throw new DomainException(ErrorCode.REVIEW_NOT_FOUND);
    }

    // 사용자 찾기
    var isExistUser = userRepository.existsById(userId);
    if(!isExistUser) {
      throw new DomainException(ErrorCode.NOT_FOUND);
    }

    boolean likedBefore = review.liked(userId);

    if (likedBefore) {
      review.decreaseLikeCount(userId);
    } else {
      review.increaseLikeCount(userId);

      log.info("좋아요 처리 시작");
      // 2) 새 좋아요가 생긴 경우에만 알림 생성
      notificationService.createNotifyByLike(reviewId, userId);
      log.info("좋아요 처리 완료");
    }

    boolean likedAfter = review.liked(userId);
    return ReviewLikeDto.builder()
            .reviewId(review.getId())
            .userId(userId)
            .liked(likedAfter)
            .build();
  }

  @Override
  public CursorPageResponseReviewDto findReviews(ReviewSearchRequestDto requestDto, UUID currentUserId) {
    // 요청 DTO에서 필요한 값 추출
    String keyword = requestDto.getKeyword();
    String orderBy = requestDto.getOrderBy();
    Direction direction = requestDto.getDirection();
    UUID userId = requestDto.getUserId();
    UUID bookId = requestDto.getBookId();
    String cursor = requestDto.getCursor();
    Instant after = requestDto.getAfter();
    int limit = requestDto.getLimit();

    // 리포지토리 메서드 호출하여 데이터 조회
    List<Review> reviews = reviewRepository.findReviewsWithCursor(
            keyword, orderBy, direction, userId, bookId, cursor, after, limit + 1
    );

    // 다음 페이지 존재 여부 확인 (N+1 조회 방식)
    boolean hasNext = reviews.size() > limit;

    // 실제 응답에 포함될 리뷰 목록 (마지막 요소는 next cursor 확인용이므로 제외)
    List<Review> responseReviews = hasNext ? reviews.subList(0, limit) : reviews;

    // 다음 페이지 커서 정보 설정
    String nextCursor = null;
    Instant nextAfter = null;

    if (hasNext && !responseReviews.isEmpty()) {
      Review lastReview = responseReviews.get(responseReviews.size() - 1);
      nextCursor =
              orderBy.equals("rating")
                      ? String.valueOf(lastReview.getRating())
                      : lastReview.getId().toString();
      nextAfter = lastReview.getCreatedAt();
    }

    // DTO로 변환
    // 썸네일 URL을 S3에서 가져오는 로직으로 수정 - jw
    List<ReviewDto> reviewDtos = responseReviews.stream()
        .map(review -> {
          Book book = review.getBook();
          String thumbnailUrl = null;

          if (book != null && book.getThumbnailUrl() != null) {
            thumbnailUrl = thumbnailImageStorage.get(book.getThumbnailUrl());
          }
          
          // 코멘트 수를 DB에서 직접 가져옵니다
          int commentCount = commentRepository.countByReviewIdAndIsDeletedFalse(review.getId());
          // 리뷰 엔티티에 코멘트 수 설정
          review.updateCommentCount(commentCount);

          return reviewMapper.toDto(review, thumbnailUrl, currentUserId);
        })
        .collect(Collectors.toList());

    // 응답 DTO 구성
    return CursorPageResponseReviewDto.builder()
            .content(reviewDtos)
            .nextCursor(nextCursor)
            .nextAfter(nextAfter)
            .hasNext(hasNext)
            .size(responseReviews.size())
            .build();
  }

  @Override
  public CursorPageResponsePopularReviewDto getPopularReviews(
      PeriodType period,
      Direction direction,
      String cursor,
      Instant after,
      Integer limit) {

    log.info("인기 리뷰 조회 시작 - 기간 : {}, 방향 : {}", period, direction);

    int size = Optional.ofNullable(limit).orElse(50);

    List<PopularReview> fetched = popularRepository.findReviewsWithCursor(period, direction,
        cursor, after, size + 1);

    if (fetched.isEmpty()) {
      log.info("조회딘 인기 리뷰가 없습니다 . - 기간 : {}", period);
      return CursorPageResponsePopularReviewDto.builder()
              .content(List.of())
              .size(0)
              .totalElements(0L)
              .hasNext(false)
              .build();
    }


    boolean hasNext = fetched.size() > size;

    List<PopularReview> responseReviews = hasNext ? fetched.subList(0, size) : fetched;

    String nextCursor = null;
    Instant nextAfter = null;

    if (hasNext && !responseReviews.isEmpty()) {
      PopularReview lastReview = responseReviews.get(responseReviews.size() - 1);
      nextCursor = String.valueOf(lastReview.getRank());
      nextAfter = lastReview.getCreatedAt();
    }

    Instant from = period.toStartInstant(Instant.now());
    long totalElements = popularRepository.countByPeriodSince(period, from);

    log.info("인기 리뷰 조회 안료 - 기간 : {}, 결과 수 : {}, 총 개수: {}",period, responseReviews.size(), totalElements);

    // DTO 변환 로직
    List<PopularReviewDto> content = responseReviews.stream()
            .map(popularReview -> {
                Review review = popularReview.getReview(); // 연관된 Review 엔티티 가져오기
                return PopularReviewDto.builder()
                        .id(popularReview.getId())
                        .reviewId(review.getId()) // 연관된 Review의 ID
                        .bookId(review.getBook().getId()) // Review를 통해 Book ID 접근
                        .bookTitle(review.getBook().getTitle()) // Review를 통해 Book Title 접근
                        .bookThumbnailUrl(thumbnailImageStorage.get(review.getBook().getThumbnailUrl())) // Review를 통해 Book Thumbnail URL 접근 -> S3 로직 추가 jw
                        .userId(review.getUser().getId()) // Review를 통해 User ID 접근
                        .userNickname(review.getUser().getNickname()) // Review를 통해 User Nickname 접근
                        .reviewContent(review.getContent())
                        .reviewRating(popularReview.getReviewRating()) // PopularReview의 평점
                        .period(popularReview.getPeriod())
                        .createdAt(popularReview.getCreatedAt()) // PopularReview의 생성 시간 (점수 계산 시간)
                        .rank(popularReview.getRank())
                        .score(popularReview.getScore())
                        .likeCount(popularReview.getLikeCount())
                        .commentCount(popularReview.getCommentCount())
                        .build();
            }).toList();

    // 빌더 패턴으로 응답 DTO 생성
    return CursorPageResponsePopularReviewDto.builder()
            .content(content)
            .nextCursor(nextCursor)
            .nextAfter(nextAfter)
            .size(content.size())
            .totalElements(totalElements)
            .hasNext(hasNext)
            .build();
  }

  @Override
  public Review findByIdEntityReturn(UUID reviewId) {
    return reviewRepository
            .findById(reviewId)
            .orElseThrow(() -> new DomainException(ErrorCode.REVIEW_NOT_FOUND));
  }

  // 도서에 관련된 집계 필드 업데이트 - jw
  private void recalculateBookStats(Book book) {
    // 도서에 작성된 리뷰 개수 조회 - jw
    int reviewCount = reviewRepository.countByBookId(book.getId());
    // 도서에 대한 평균 평점을 계산 - jw
    double rating = reviewRepository.calculateAverageRatingByBookId(book.getId());
    // 조회된 리뷰 개수와 평균 평점을 Book 엔티티에 반영 - jw
    book.updateReviewStatus(reviewCount, rating);
  }
}
