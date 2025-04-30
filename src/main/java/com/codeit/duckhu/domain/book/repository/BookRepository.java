package com.codeit.duckhu.domain.book.repository;

import com.codeit.duckhu.domain.book.entity.Book;
import java.util.List;
import java.util.UUID;
import org.apache.catalina.LifecycleState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book, UUID>, BookRepositoryCustom {

  boolean existsByIsbn(String isbn);

  // 도서에 리뷰가 1개 이상인 도서만 가져오기
  @Query("SELECT b FROM Book b WHERE b.isDeleted = false AND b.reviewCount > 0")
  List<Book> findBooksWithReviews();
}
