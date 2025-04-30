package com.codeit.duckhu.domain.book.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.codeit.duckhu.domain.book.entity.Book;
import com.codeit.duckhu.domain.review.entity.Review;
import com.codeit.duckhu.domain.review.repository.ReviewRepository;
import com.codeit.duckhu.domain.review.repository.TestJpaConfig;
import com.codeit.duckhu.domain.user.entity.User;
import com.codeit.duckhu.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({TestJpaConfig.class})
@ActiveProfiles("test")
public class BookReviewTest {
  @Autowired private ReviewRepository reviewRepository;

  @Autowired private BookRepository bookRepository;

  @Autowired private UserRepository userRepository;

  @Test
  @DisplayName("리뷰가 존재하면 개수와 평균 평점을 정확히 계산한다")
  void countAndAverageRating_withReviews() {
    // Given
    Book book =
        Book.builder()
            .title("테스트 도서")
            .author("저자")
            .publisher("출판사")
            .description("설명")
            .publishedDate(LocalDate.of(2022, 1, 1))
            .isDeleted(false)
            .build();
    bookRepository.save(book);

    User user1 = User.builder().email("a@test.com").nickname("a").password("1234").build();
    User user2 = User.builder().email("b@test.com").nickname("b").password("1234").build();
    userRepository.saveAll(List.of(user1, user2));

    Review r1 =
        Review.builder()
            .book(book)
            .user(user1)
            .rating(4)
            .content("좋아요")
            // .isDeleted(false)
            .build();

    Review r2 =
        Review.builder()
            .book(book)
            .user(user2)
            .rating(2)
            .content("그저 그래요")
            // .isDeleted(false)
            .build();

    reviewRepository.saveAll(List.of(r1, r2));

    // When
    int count = reviewRepository.countByBookId(book.getId());
    double avg = reviewRepository.calculateAverageRatingByBookId(book.getId());

    // Then
    assertThat(count).isEqualTo(2);
    assertThat(avg).isEqualTo(3.0); // (4 + 2) / 2
  }

  @Test
  @DisplayName("리뷰가 없으면 개수는 0, 평균 평점은 0.0이다")
  void countAndAverageRating_withoutReviews() {
    // Given
    Book book =
        Book.builder()
            .title("리뷰 없는 도서")
            .author("저자")
            .publisher("출판사")
            .description("설명")
            .publishedDate(LocalDate.of(2023, 1, 1))
            .isDeleted(false)
            .build();
    bookRepository.save(book);

    // When
    int count = reviewRepository.countByBookId(book.getId());
    double avg = reviewRepository.calculateAverageRatingByBookId(book.getId());

    // Then
    assertThat(count).isEqualTo(0);
    assertThat(avg).isEqualTo(0.0);
  }
}
