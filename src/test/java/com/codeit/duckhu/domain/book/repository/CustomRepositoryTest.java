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

  /**
   * 테스트 시작 전, 데이터베이스에 테스트용 도서 5권을 저장합니다.
   */
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

  /**
   * 제목에 특정 키워드가 포함된 도서를 검색하는 기능을 검증합니다.
   * - 키워드가 포함된 도서가 모두 조회되어야 합니다.
   */
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

  /**
   * 출판일(publishedDate) 기준으로 도서를 내림차순(DESC) 정렬하는 기능을 검증합니다.
   * - 가장 최근 출판된 도서가 먼저 나와야 합니다.
   */
  @Test
  @DisplayName("출판일 기준 내림차순 정렬")
  void sortBooksByPublishedDateDesc() {
    List<Book> books =
        bookRepository.searchBooks(null, "publishedDate", Direction.DESC, null, null, 10);
    assertThat(books.get(0).getPublishedDate()).isAfter(books.get(1).getPublishedDate());
  }

  /**
   * 제목(title) 기준 오름차순(ASC) 정렬 + 커서 기반 페이지네이션 기능을 검증합니다.
   * - 첫 페이지 조회 후, 커서를 사용하여 다음 페이지 조회가 가능해야 합니다.
   */
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
