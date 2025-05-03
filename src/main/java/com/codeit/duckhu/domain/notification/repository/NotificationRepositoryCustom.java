package com.codeit.duckhu.domain.notification.repository;

import com.codeit.duckhu.domain.notification.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface NotificationRepositoryCustom {
  void deleteOldConfirmedNotifications(Instant cutoff);

  void bulkMarkAsConfirmed(UUID receiverId, Instant now);

  List<Notification> findDescNoCursor(UUID receiverId, Pageable pageable);

  List<Notification> findDescWithCursor(UUID receiverId, Instant cursor, Pageable pageable);

  List<Notification> findAscNoCursor(UUID receiverId, Pageable pageable);

  List<Notification> findAscWithCursor(UUID receiverId, Instant cursor, Pageable pageable);
}
