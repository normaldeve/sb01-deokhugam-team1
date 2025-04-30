package com.codeit.duckhu.domain.book.mapper;

import com.codeit.duckhu.domain.book.dto.BookDto;
import com.codeit.duckhu.domain.book.entity.Book;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookMapper {

  BookDto toDto(Book book);

  default BookDto toDto(Book book, String thumbnailUrl) {
    return new BookDto(
        book.getId(),
        book.getTitle(),
        book.getAuthor(),
        book.getDescription(),
        book.getPublisher(),
        book.getPublishedDate(),
        book.getIsbn(),
        thumbnailUrl,
        book.getReviewCount(),
        book.getRating(),
        book.getCreatedAt(),
        book.getUpdatedAt());
  }
}
