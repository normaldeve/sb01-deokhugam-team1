package com.codeit.duckhu.domain.book.dto;

import java.util.List;

public record NaverApiResponse(List<Item> items) {
  public record Item(
      String title,
      String author,
      String description,
      String publisher,
      String pubdate,
      String isbn,
      String image) {}
}
