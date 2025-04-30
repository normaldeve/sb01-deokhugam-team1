package com.codeit.duckhu.domain.notification.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    UUID userId,
    UUID reviewId,
    String reviewTitle, // 추후 확장
    String content,
    boolean confirmed,
    Instant createdAt,
    Instant updatedAt) {}
