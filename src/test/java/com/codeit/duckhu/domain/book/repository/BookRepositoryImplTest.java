package com.codeit.duckhu.domain.book.repository;

import com.codeit.duckhu.domain.book.entity.Book;
import com.codeit.duckhu.domain.book.repository.BookRepositoryImpl;
import com.codeit.duckhu.domain.review.repository.TestJpaConfig;
import com.codeit.duckhu.global.type.Direction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({BookRepositoryImpl.class, TestJpaConfig.class})
@Sql("/books.sql") // ✅ books 데이터셋 로드
@DisplayName("BookRepositoryImpl 테스트")
public class BookRepositoryImplTest {

  @Autowired
  private BookRepositoryImpl bookRepositoryImpl;

  @PersistenceContext
  private EntityManager em;

  @Nested
  @DisplayName("도서 검색")
  class SearchBooks {

    @Test
    @DisplayName("키워드 검색 - 제목으로 검색 성공")
    void searchByTitle() {
      List<Book> result = bookRepositoryImpl.searchBooks("Effective", "title", Direction.ASC, null, null, 10);
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getTitle()).contains("Effective Java");
    }

    @Test
    @DisplayName("키워드 검색 - 저자로 검색 성공")
    void searchByAuthor() {
      List<Book> result = bookRepositoryImpl.searchBooks("Robert", "title", Direction.ASC, null, null, 10);
      assertThat(result).hasSize(2); // Clean Code, Clean Architecture
      assertThat(result).extracting(Book::getAuthor).allMatch(author -> author.contains("Robert"));
    }

    @Test
    @DisplayName("키워드 검색 - ISBN으로 검색 성공")
    void searchByIsbn() {
      List<Book> result = bookRepositoryImpl.searchBooks("978-0132350884", "title", Direction.ASC, null, null, 10);
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getIsbn()).isEqualTo("978-0132350884");
    }

    @Test
    @DisplayName("정렬 테스트 - 제목 오름차순 정렬")
    void sortByTitleAsc() {
      List<Book> result = bookRepositoryImpl.searchBooks("", "title", Direction.ASC, null, null, 10);
      assertThat(result).hasSizeGreaterThanOrEqualTo(3);
      assertThat(result).extracting(Book::getTitle)
          .isSortedAccordingTo(String::compareToIgnoreCase);
    }

    @Test
    @DisplayName("정렬 테스트 - 제목 내림차순 정렬")
    void sortByTitleDesc() {
      List<Book> result = bookRepositoryImpl.searchBooks("", "title", Direction.DESC, null, null, 10);
      assertThat(result).hasSizeGreaterThanOrEqualTo(3);
      assertThat(result).extracting(Book::getTitle)
          .isSortedAccordingTo((a, b) -> b.compareToIgnoreCase(a));
    }
  }

  @Nested
  @DisplayName("커서 기반 페이지네이션 테스트")
  class CursorPagingTest {

    @Test
    @DisplayName(" rating 기준 커서 페이징")
    void cursorPagingByRating() {
      List<Book> books = bookRepositoryImpl.searchBooks(
          "", "rating", Direction.ASC, "4.5", null, 3
      );

      assertThat(books).isNotEmpty()
          .extracting(Book::getRating)
          .allSatisfy(r -> assertThat(r).isLessThanOrEqualTo(4.5));
    }

    @Test
    @DisplayName(" reviewCount 기준 커서 페이징")
    void cursorPagingByReviewCount() {
      List<Book> books = bookRepositoryImpl.searchBooks(
          "", "reviewCount", Direction.ASC, "60", null, 3
      );

      assertThat(books).isNotEmpty()
          .extracting(Book::getReviewCount)
          .allSatisfy(count -> assertThat(count).isLessThanOrEqualTo(60));
    }

    @Test
    @DisplayName("publishedDate 기준 커서 페이징")
    void cursorPagingByPublishedDate() {
      List<Book> books = bookRepositoryImpl.searchBooks(
          "", "publishedDate", Direction.ASC,
          "2018-01-06", null, 3
      );

      assertThat(books).isNotEmpty()
          .extracting(Book::getPublishedDate)
          .allSatisfy(date -> assertThat(date).isBeforeOrEqualTo(LocalDate.of(2018, 1, 6)));
    }
  }

}
