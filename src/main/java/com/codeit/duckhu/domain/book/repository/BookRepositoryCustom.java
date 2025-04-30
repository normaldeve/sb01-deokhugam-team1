package com.codeit.duckhu.domain.book.repository;

import com.codeit.duckhu.domain.book.entity.Book;
import com.codeit.duckhu.global.type.Direction;
import java.time.Instant;
import java.util.List;

public interface BookRepositoryCustom {
  List<Book> searchBooks(
      String keyword, String orderBy, Direction direction, String cursor, Instant after, int limit);
}
