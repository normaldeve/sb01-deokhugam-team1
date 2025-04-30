package com.codeit.duckhu.domain.book.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BookDto(
    UUID id,
    String title,
    String author,
    String description,
    String publisher,
    LocalDate publishedDate,
    String isbn,
    String thumbnailUrl,
    Integer reviewCount,
    Double rating,
    Instant createdAt,
    Instant updatedAt) {}
