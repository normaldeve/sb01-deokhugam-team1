package com.codeit.duckhu.domain.book.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.codeit.duckhu.domain.book.entity.Book;
import com.codeit.duckhu.domain.review.repository.TestJpaConfig;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class BookRepositoryTest {

  @Autowired private BookRepository bookRepository;

  @Nested
  @DisplayName("Book 저장 테스트")
  class SaveBookTest {

    private final String duplicatedIsbn = "9780134685991";

    @BeforeEach
    void setUp() {
      Book book =
          Book.builder()
              .title("Effective Java")
              .author("Joshua Bloch")
              .description("Java best practices")
              .publisher("Addison-Wesley")
              .publishedDate(LocalDate.now())
              .isbn(duplicatedIsbn)
              .thumbnailUrl("https://example.com/thumb.jpg")
              .isDeleted(false)
              .build();

      bookRepository.save(book);
    }

    @Test
    @DisplayName("책 엔티티를 저장하고 조회할 수 있다.")
    void save_success() {
      // given
      String isbn = "9780134685992";
      Book newBook =
          Book.builder()
              .title("Java Concurrency in Practice")
              .author("Brian Goetz")
              .description("Concurrency concepts in Java")
              .publisher("Addison-Wesley")
              .publishedDate(LocalDate.now())
              .isbn(isbn)
              .thumbnailUrl("https://example.com/thumb2.jpg")
              .isDeleted(false)
              .build();

      // when
      Book saved = bookRepository.save(newBook);
      Optional<Book> found = bookRepository.findById(saved.getId());

      // then
      assertThat(found).isPresent();
      assertThat(found.get().getTitle()).isEqualTo("Java Concurrency in Practice");
    }

    @Test
    @DisplayName("동일한 ISBN으로 저장 시 예외가 발생한다.")
    void save_duplicateIsbn_throwsException() {
      // given
      Book duplicateBook =
          Book.builder()
              .title("Duplicate Book")
              .author("Someone Else")
              .description("This should fail")
              .publisher("Unknown")
              .publishedDate(LocalDate.now())
              .isbn(duplicatedIsbn) // 중복된 ISBN
              .thumbnailUrl("https://example.com/thumb3.jpg")
              .isDeleted(false)
              .build();

      // when & then
      assertThrows(
          DataIntegrityViolationException.class,
          () -> {
            bookRepository.saveAndFlush(duplicateBook);
          });
    }
  }
}
