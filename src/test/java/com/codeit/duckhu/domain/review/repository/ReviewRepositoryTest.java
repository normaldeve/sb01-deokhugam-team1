package com.codeit.duckhu.domain.review.repository;

import static com.codeit.duckhu.global.type.Direction.DESC;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.codeit.duckhu.domain.book.entity.Book;
import com.codeit.duckhu.domain.book.repository.BookRepository;
import com.codeit.duckhu.domain.review.entity.Review;
import com.codeit.duckhu.domain.user.entity.User;
import com.codeit.duckhu.domain.user.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
public class ReviewRepositoryTest {

  @Autowired private ReviewRepository reviewRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private BookRepository bookRepository;

  @Test
  @DisplayName("리뷰 저장 확인")
  void saveReview_shouldSavedReview() {
    // Given
    User user =
        User.builder().email("test@example.com").nickname("테스터").password("password").build();
    User savedUser = userRepository.save(user);

    Book book =
        Book.builder()
            .title("테스트 도서")
            .author("테스트 작가")
            .publisher("테스트 출판사")
            .isbn("9788956609959")
            .publishedDate(LocalDate.now())
            .build();
    Book savedBook = bookRepository.save(book);

    Review savedReview =
        Review.builder()
            .rating(5)
            .content("재밌어요")
            .likeCount(0)
            .commentCount(0)
            .user(savedUser)
            .book(savedBook)
            .build();

    // When
    Review foundReview = reviewRepository.save(savedReview);

    // Then
    assertThat(foundReview).isNotNull();
    assertThat(foundReview.getRating()).isEqualTo(5);
    assertThat(foundReview.getContent()).isEqualTo("재밌어요");
    assertThat(foundReview.getLikeCount()).isEqualTo(0);
    assertThat(foundReview.getCommentCount()).isEqualTo(0);
  }

  @Test
  @DisplayName("사용자 ID와 도서 ID로 리뷰 찾기")
  void findByUserIdAndBookId_success() {
    // Given
    // 사용자 생성 및 저장
    User user =
        User.builder()
            .email("test@example.com")
            .nickname("테스터")
            .password("password")
            // .isDeleted(false)
            .build();
    User savedUser = userRepository.save(user);

    // 도서 생성 및 저장
    Book book =
        Book.builder()
            .title("테스트 도서")
            .author("테스트 작가")
            .publisher("테스트 출판사")
            .isbn("9788956609959")
            .publishedDate(LocalDate.now())
            .isDeleted(false)
            .description("테스트 설명")
            .thumbnailUrl("http://example.com/image.jpg")
            .build();
    Book savedBook = bookRepository.save(book);

    // 리뷰 생성 및 저장
    Review review =
        Review.builder()
            .content("정말 좋은 책이에요")
            .rating(5)
            .likeCount(0)
            .commentCount(0)
            .user(savedUser)
            .book(savedBook)
            .build();
    reviewRepository.save(review);

    // When
    Optional<Review> foundReview =
        reviewRepository.findByUserIdAndBookId(savedUser.getId(), savedBook.getId());

    // Then
    assertThat(foundReview.isPresent()).isTrue();
    assertThat(foundReview.get().getContent()).isEqualTo("정말 좋은 책이에요");
    assertThat(foundReview.get().getRating()).isEqualTo(5);
    assertThat(foundReview.get().getUser().getId()).isEqualTo(savedUser.getId());
    assertThat(foundReview.get().getBook().getId()).isEqualTo(savedBook.getId());
  }

  @Test
  @DisplayName("사용자 ID와 도서 ID로 리뷰 찾기 - 리뷰 없음")
  void findByUserIdAndBookId_reviewNotFound() {
    // Given
    // 사용자 생성 및 저장
    User user =
        User.builder()
            .email("test2@example.com")
            .nickname("테스터2")
            .password("password")
            // .isDeleted(false)
            .build();
    User savedUser = userRepository.save(user);

    // 도서 생성 및 저장
    Book book =
        Book.builder()
            .title("테스트 도서2")
            .author("테스트 작가2")
            .publisher("테스트 출판사2")
            .isbn("9788956609958")
            .publishedDate(LocalDate.now())
            .isDeleted(false)
            .description("테스트 설명2")
            .thumbnailUrl("http://example.com/image2.jpg")
            .build();
    Book savedBook = bookRepository.save(book);

    // 리뷰는 저장하지 않음

    // When
    Optional<Review> foundReview =
        reviewRepository.findByUserIdAndBookId(savedUser.getId(), savedBook.getId());

    // Then
    assertThat(foundReview.isPresent()).isFalse();
  }

  @Nested
  @DisplayName("커서 페이지네이션")
  class CursorPaginationTests {
    @Test
    @DisplayName("커서 기반 페이지네이션으로 리뷰 목록 조회 (시간순)")
    void findReviewsWithCursor_byCreatedAt_success() {
      // Given
      // 테스트 사용자 생성
      User user =
          User.builder()
              .email("test-cursor1@example.com")
              .nickname("커서테스터1")
              .password("password")
              //        .isDeleted(false)
              .build();
      User savedUser = userRepository.save(user);

      // 테스트 도서 생성
      Book book =
          Book.builder()
              .title("커서 테스트 도서")
              .author("테스트 작가")
              .publisher("테스트 출판사")
              .isbn("9788956609950")
              .publishedDate(LocalDate.now())
              .isDeleted(false)
              .build();
      Book savedBook = bookRepository.save(book);

      // 리뷰 5개 생성
      for (int i = 1; i <= 5; i++) {
        Review review =
            Review.builder()
                .content("커서 페이지네이션 테스트 리뷰 " + i)
                .rating(i)
                .likeCount(0)
                .commentCount(0)
                .user(savedUser)
                .book(savedBook)
                .build();
        reviewRepository.save(review);
      }

      // When
      List<Review> firstPage =
          reviewRepository.findReviewsWithCursor(
              null, // 키워드 없음
              "createdAt", // 생성 시간 기준 정렬
              DESC, // 내림차순
              null, // 사용자 ID 필터 없음
              null, // 책 ID 필터 없음
              null, // 첫 페이지니까 커서 없음
              null, // 첫 페이지니까 after 없음
              3 // 페이지 크기 3
              );

      // Then
      assertThat(firstPage.size()).isEqualTo(3);
      assertThat(firstPage.get(0).getContent()).contains("커서 페이지네이션 테스트 리뷰 5");
      assertThat(firstPage.get(1).getContent()).contains("커서 페이지네이션 테스트 리뷰 4");
      assertThat(firstPage.get(2).getContent()).contains("커서 페이지네이션 테스트 리뷰 3");

      // 다음 페이지 조회
      Review lastReviewOfFirstPage = firstPage.get(firstPage.size() - 1);
      List<Review> secondPage =
          reviewRepository.findReviewsWithCursor(
              null, // 키워드 없음
              "createdAt", // 생성 시간 기준 정렬
              DESC, // 내림차순
              null, // 사용자 ID 필터 없음
              null, // 책 ID 필터 없음
              lastReviewOfFirstPage.getId().toString(), // 마지막 리뷰 ID를 커서로 사용
              lastReviewOfFirstPage.getCreatedAt() != null
                  ? lastReviewOfFirstPage.getCreatedAt()
                  : Instant.now(), // 마지막 리뷰 생성 시간을 after로 사용
              3 // 페이지 크기 3
              );

      // Then
      assertThat(secondPage.size()).isEqualTo(2);
      assertThat(secondPage.get(0).getContent()).contains("커서 페이지네이션 테스트 리뷰 2");
      assertThat(secondPage.get(1).getContent()).contains("커서 페이지네이션 테스트 리뷰 1");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션으로 리뷰 목록 조회 (평점순)")
    void findReviewsWithCursor_byRating_success() {
      // Given
      // 테스트 사용자 생성
      User user =
          User.builder()
              .email("test-cursor2@example.com")
              .nickname("커서테스터2")
              .password("password")
              //            .isDeleted(false)
              .build();
      User savedUser = userRepository.save(user);

      // 테스트 도서 생성
      Book book =
          Book.builder()
              .title("평점 테스트 도서")
              .author("테스트 작가")
              .publisher("테스트 출판사")
              .isbn("9788956609951")
              .publishedDate(LocalDate.now())
              .isDeleted(false)
              .build();
      Book savedBook = bookRepository.save(book);

      // 리뷰 5개 생성 (평점 5, 4, 3, 2, 1)
      for (int i = 5; i >= 1; i--) {
        Review review =
            Review.builder()
                .content("평점 " + i + "점 테스트 리뷰")
                .rating(i)
                .likeCount(0)
                .commentCount(0)
                .user(savedUser)
                .book(savedBook)
                .build();
        reviewRepository.save(review);
      }

      // When
      List<Review> firstPage =
          reviewRepository.findReviewsWithCursor(
              null, // 키워드 없음
              "rating", // 평점 기준 정렬
              DESC, // 내림차순
              null, // 사용자 ID 필터 없음
              null, // 책 ID 필터 없음
              null, // 첫 페이지니까 커서 없음
              null, // 첫 페이지니까 after 없음
              3 // 페이지 크기 3
              );

      // Then
      assertThat(firstPage.size()).isEqualTo(3);
      assertThat(firstPage.get(0).getRating()).isEqualTo(5);
      assertThat(firstPage.get(1).getRating()).isEqualTo(4);
      assertThat(firstPage.get(2).getRating()).isEqualTo(3);

      // 다음 페이지 조회
      Review lastReviewOfFirstPage = firstPage.get(firstPage.size() - 1);
      List<Review> secondPage =
          reviewRepository.findReviewsWithCursor(
              null, // 키워드 없음
              "rating", // 평점 기준 정렬
              DESC, // 내림차순
              null, // 사용자 ID 필터 없음
              null, // 책 ID 필터 없음
              String.valueOf(lastReviewOfFirstPage.getRating()), // 마지막 리뷰 평점을 커서로 사용
              lastReviewOfFirstPage.getCreatedAt() != null
                  ? lastReviewOfFirstPage.getCreatedAt()
                  : Instant.now(), // 마지막 리뷰 생성 시간을 after로 사용
              3 // 페이지 크기 3
              );

      // Then
      assertThat(secondPage.size()).isEqualTo(2);
      assertThat(secondPage.get(0).getRating()).isEqualTo(2);
      assertThat(secondPage.get(1).getRating()).isEqualTo(1);
    }

    @Test
    @DisplayName("키워드로 리뷰 검색 및 커서 페이지네이션")
    void findReviewsWithCursor_withKeyword_success() {
      // Given
      // 테스트 사용자 생성
      User user =
          User.builder()
              .email("test-keyword@example.com")
              .nickname("키워드테스터")
              .password("password")
              //            .isDeleted(false)
              .build();
      User savedUser = userRepository.save(user);

      // 테스트 도서 생성
      Book book =
          Book.builder()
              .title("키워드 테스트 도서")
              .author("테스트 작가")
              .publisher("테스트 출판사")
              .isbn("9788956609952")
              .publishedDate(LocalDate.now())
              .isDeleted(false)
              .build();
      Book savedBook = bookRepository.save(book);

      // 검색될 리뷰 3개 생성
      for (int i = 1; i <= 3; i++) {
        Review review =
            Review.builder()
                .content("검색용 키워드가 포함된 리뷰 " + i)
                .rating(i)
                .likeCount(0)
                .commentCount(0)
                .user(savedUser)
                .book(savedBook)
                .build();
        reviewRepository.save(review);
      }

      // 검색되지 않을 리뷰 2개 생성
      for (int i = 1; i <= 2; i++) {
        Review review =
            Review.builder()
                .content("다른 내용의 리뷰 " + i)
                .rating(i)
                .likeCount(0)
                .commentCount(0)
                .user(savedUser)
                .book(savedBook)
                .build();
        reviewRepository.save(review);
      }

      // When
      List<Review> searchResult =
          reviewRepository.findReviewsWithCursor(
              "검색용 키워드", // 검색 키워드
              "createdAt", // 생성 시간 기준 정렬
              DESC, // 내림차순
              null, // 사용자 ID 필터 없음
              null, // 책 ID 필터 없음
              null, // 첫 페이지니까 커서 없음
              null, // 첫 페이지니까 after 없음
              10 // 페이지 크기 10
              );

      // Then
      assertThat(searchResult.size()).isEqualTo(3);
      assertThat(searchResult.get(0).getContent()).contains("검색용 키워드");
    }

    @Test
    @DisplayName("특정 사용자의 리뷰만 커서 페이지네이션으로 조회")
    void findReviewsWithCursor_byUserId_success() {
      // Given
      // 테스트 사용자 2명 생성
      User user1 =
          User.builder()
              .email("test-user1@example.com")
              .nickname("사용자1")
              .password("password")
              //            .isDeleted(false)
              .build();
      User savedUser1 = userRepository.save(user1);

      User user2 =
          User.builder()
              .email("test-user2@example.com")
              .nickname("사용자2")
              .password("password")
              //            .isDeleted(false)
              .build();
      User savedUser2 = userRepository.save(user2);

      // 테스트 도서 생성
      Book book =
          Book.builder()
              .title("사용자 필터 테스트 도서")
              .author("테스트 작가")
              .publisher("테스트 출판사")
              .isbn("9788956609953")
              .publishedDate(LocalDate.now())
              .isDeleted(false)
              .build();
      Book savedBook = bookRepository.save(book);

      // 사용자1의 리뷰 3개 생성
      for (int i = 1; i <= 3; i++) {
        Review review =
            Review.builder()
                .content("사용자1의 리뷰 " + i)
                .rating(i)
                .likeCount(0)
                .commentCount(0)
                .user(savedUser1)
                .book(savedBook)
                .build();
        reviewRepository.save(review);
      }

      // 사용자2의 리뷰 2개 생성
      for (int i = 1; i <= 2; i++) {
        Review review =
            Review.builder()
                .content("사용자2의 리뷰 " + i)
                .rating(i)
                .likeCount(0)
                .commentCount(0)
                .user(savedUser2)
                .book(savedBook)
                .build();
        reviewRepository.save(review);
      }

      // When
      List<Review> user1Reviews =
          reviewRepository.findReviewsWithCursor(
              null, // 키워드 없음
              "createdAt", // 생성 시간 기준 정렬
              DESC, // 내림차순
              savedUser1.getId(), // 사용자1의 ID로 필터링
              null, // 책 ID 필터 없음
              null, // 첫 페이지니까 커서 없음
              null, // 첫 페이지니까 after 없음
              10 // 페이지 크기 10
              );

      // Then
      assertThat(user1Reviews.size()).isEqualTo(3);
      for (Review review : user1Reviews) {
        assertThat(review.getUser().getId()).isEqualTo(savedUser1.getId());
        assertThat(review.getContent()).contains("사용자1의 리뷰");
      }
    }

    @Test
    @DisplayName("특정 도서의 리뷰만 커서 페이지네이션으로 조회")
    void findReviewsWithCursor_byBookId_success() {
      // Given
      // 테스트 사용자 생성
      User user =
          User.builder()
              .email("test-book@example.com")
              .nickname("도서테스터")
              .password("password")
              //            .isDeleted(false)
              .build();
      User savedUser = userRepository.save(user);

      // 테스트 도서 2개 생성
      Book book1 =
          Book.builder()
              .title("도서1")
              .author("테스트 작가")
              .publisher("테스트 출판사")
              .isbn("9788956609954")
              .publishedDate(LocalDate.now())
              .isDeleted(false)
              .build();
      Book savedBook1 = bookRepository.save(book1);

      Book book2 =
          Book.builder()
              .title("도서2")
              .author("테스트 작가")
              .publisher("테스트 출판사")
              .isbn("9788956609955")
              .publishedDate(LocalDate.now())
              .isDeleted(false)
              .build();
      Book savedBook2 = bookRepository.save(book2);

      // 도서1에 리뷰 3개 생성
      for (int i = 1; i <= 3; i++) {
        Review review =
            Review.builder()
                .content("도서1 리뷰 " + i)
                .rating(i)
                .likeCount(0)
                .commentCount(0)
                .user(savedUser)
                .book(savedBook1)
                .build();
        reviewRepository.save(review);
      }

      // 도서2에 리뷰 2개 생성
      for (int i = 1; i <= 2; i++) {
        Review review =
            Review.builder()
                .content("도서2 리뷰 " + i)
                .rating(i)
                .likeCount(0)
                .commentCount(0)
                .user(savedUser)
                .book(savedBook2)
                .build();
        reviewRepository.save(review);
      }

      // When
      List<Review> book1Reviews =
          reviewRepository.findReviewsWithCursor(
              null, // 키워드 없음
              "createdAt", // 생성 시간 기준 정렬
              DESC, // 내림차순
              null, // 사용자 ID 필터 없음
              savedBook1.getId(), // 도서1의 ID로 필터링
              null, // 첫 페이지니까 커서 없음
              null, // 첫 페이지니까 after 없음
              10 // 페이지 크기 10
              );

      // Then
      assertThat(book1Reviews.size()).isEqualTo(3);
      for (Review review : book1Reviews) {
        assertThat(review.getBook().getId()).isEqualTo(savedBook1.getId());
        assertThat(review.getContent()).contains("도서1 리뷰");
      }
    }
    @Test
    @DisplayName("평점 정렬인데 커서 없음, after만 있는 경우")
    void findReview_ratingSort_afterOnly() {
      List<Review> result = reviewRepository.findReviewsWithCursor(
          null, "rating", DESC, null, null, null, Instant.now(), 5
      );
      assertThat(result).isNotNull(); // 오류 없이 실행되는지 확인
    }

    @Test
    @DisplayName("잘못된 orderBy 필드가 들어온 경우")
    void findReview_invalidOrderBy() {
      List<Review> result = reviewRepository.findReviewsWithCursor(
          null, "invalidField", DESC, null, null, null, null, 5
      );
      assertThat(result).isNotNull(); // 기본 정렬이 작동하는지만 확인
    }
  }
}
