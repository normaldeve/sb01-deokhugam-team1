package com.codeit.duckhu.domain.notification.dto;

import jakarta.validation.constraints.NotNull;

public record NotificationUpdateRequest(@NotNull(message = "알림 확인 상태는 필수입니다.") boolean confirmed) {}
