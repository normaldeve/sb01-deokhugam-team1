package com.codeit.duckhu.domain.book.dto;

import java.time.Instant;
import java.util.UUID;

public record PopularBookDto(
    UUID id,
    UUID bookId,
    String title,
    String author,
    String thumbnailUrl,
    String period,
    Integer rank,
    Double score,
    Integer reviewCount,
    Double rating,
    Instant createdAt) {}
