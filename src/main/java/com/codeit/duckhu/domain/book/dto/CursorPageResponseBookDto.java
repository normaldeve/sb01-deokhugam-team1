package com.codeit.duckhu.domain.book.dto;

import java.time.Instant;
import java.util.List;

public record CursorPageResponseBookDto(
    List<BookDto> content,
    String nextCursor,
    Instant nextAfter,
    int size,
    int totalElements,
    Boolean hasNext) {}
