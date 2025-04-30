package com.codeit.duckhu.domain.book.repository;

import static org.assertj.core.api.Assertions.*;

import com.codeit.duckhu.domain.book.entity.Book;
import com.codeit.duckhu.domain.review.repository.TestJpaConfig;
import com.codeit.duckhu.global.type.Direction;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({TestJpaConfig.class})
public class CustomRepositoryTest {

  @Autowired private BookRepository bookRepository;

  @Autowired private EntityManager em;

  private final String keyword = "Effective";

  @BeforeEach
  void setUp() {
    for (int i = 1; i <= 5; i++) {
      Book book =
          Book.builder()
              .title("Effective Java Vol." + i)
              .author("Joshua Bloch")
              .description("Test Book " + i)
              .publisher("Addison-Wasley")
              .publishedDate(LocalDate.of(2023, 12, i))
              .isbn("97801346859" + i)
              .reviewCount(0)
              .rating(0.0)
              .isDeleted(false)
              .build();

      bookRepository.save(book);
    }
    em.flush();
    em.clear();
  }

  @Test
  @DisplayName("제목 키워드로 검색")
  void searchBooksByKeyword() {
    // given
    // when
    List<Book> books = bookRepository.searchBooks(keyword, "title", Direction.ASC, null, null, 10);

    // then
    assertThat(books).hasSize(5);
    assertThat(books.get(0).getTitle()).contains(keyword);
  }

  @Test
  @DisplayName("출판일 기준 내림차순 정렬")
  void sortBooksByPublishedDateDesc() {
    List<Book> books =
        bookRepository.searchBooks(null, "publishedDate", Direction.DESC, null, null, 10);
    assertThat(books.get(0).getPublishedDate()).isAfter(books.get(1).getPublishedDate());
  }

  @Test
  @DisplayName("제목 기준 ASC 정렬 + 커서 기반 페이지네이션")
  void paginateByTitleAsc() {
    // given
    List<Book> page1 = bookRepository.searchBooks(null, "title", Direction.ASC, null, null, 2);
    // when
    String nextCursor = page1.get(1).getTitle();
    Instant nextAfter = page1.get(1).getCreatedAt();
    List<Book> page2 =
        bookRepository.searchBooks(null, "title", Direction.ASC, nextCursor, nextAfter, 2);

    // then
    assertThat(page2).hasSize(2);
    assertThat(page2.get(0).getTitle()).isGreaterThan(page1.get(1).getTitle());
  }
}
