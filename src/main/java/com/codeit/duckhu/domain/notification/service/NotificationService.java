package com.codeit.duckhu.domain.notification.service;

import com.codeit.duckhu.domain.notification.dto.CursorPageResponseNotificationDto;
import com.codeit.duckhu.domain.notification.dto.NotificationDto;
import com.codeit.duckhu.global.type.PeriodType;
import java.time.Instant;
import java.util.UUID;

public interface NotificationService {

  // 좋아요 누를시 알림 생성
  NotificationDto createNotifyByLike(UUID reviewId, UUID triggerUserId);

  // 댓글 생성시 알림 생성
  NotificationDto createNotifyByComment(UUID reviewId, UUID triggerUserId, String comment);

  // 인기 리뷰 10등 안에 진입시 알림 생성
  NotificationDto createNotifyByPopularReview(UUID reviewId, UUID receiverId, PeriodType period,
      int rank);

  NotificationDto updateConfirmedStatus(UUID notificationId, UUID receiverId, boolean confirmed);

  void updateAllConfirmedStatus(UUID receiverId);

  // 사용자가 확인을 한 일주일을 지난 알림들 삭제
  void deleteConfirmedNotificationsOlderThanAWeek();

  // 목록 조회를 위한 메서드
  CursorPageResponseNotificationDto getNotifications(UUID receiverId, String direction, Instant cursor, int limit);
}
