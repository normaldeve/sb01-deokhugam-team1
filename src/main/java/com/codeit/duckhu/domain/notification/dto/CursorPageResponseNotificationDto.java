package com.codeit.duckhu.domain.notification.dto;

import java.time.Instant;
import java.util.List;

public record CursorPageResponseNotificationDto(
    List<NotificationDto> content,
    String nextCursor,
    Instant nextAfter,
    int size,
    long totalElements,
    boolean hasNext) {}
