package com.codeit.duckhu.domain.book.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.codeit.duckhu.domain.book.entity.Book;
import com.codeit.duckhu.domain.book.entity.PopularBook;
import com.codeit.duckhu.domain.book.repository.popular.PopularBookRepository;
import com.codeit.duckhu.domain.review.repository.TestJpaConfig;
import com.codeit.duckhu.global.type.PeriodType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class PopularBookRepositoryTest {

  @Autowired private BookRepository bookRepository;

  @Autowired private PopularBookRepository popularBookRepository;

  private Book savedBook;

  @BeforeEach
  void setUp() {
    Book book =
        Book.builder()
            .title("클린 코드")
            .author("Robert C. Martin")
            .description("클린 코드를 작성하는 방법에 대해서 알려드림")
            .publisher("Prentice Hall")
            .publishedDate(LocalDate.now())
            .isbn("1710032318812")
            .thumbnailUrl("https://example.com/clean.jpg")
            .isDeleted(false)
            .build();

    savedBook = bookRepository.saveAndFlush(book);
  }

  @Test
  @DisplayName("인기 도서를 정상적으로 저장하고 조회할 수 있어야 한다.")
  void should_save_and_retrieve_popular_book() {
    PopularBook popularBook =
        PopularBook.builder()
            .book(savedBook)
            .period(PeriodType.MONTHLY)
            .reviewCount(42)
            .rating(4.7)
            .rank(1)
            .score(92.5)
            .build();

    // when
    PopularBook saved = popularBookRepository.save(popularBook);

    // then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getBook().getTitle()).isEqualTo("클린 코드");
  }
}
