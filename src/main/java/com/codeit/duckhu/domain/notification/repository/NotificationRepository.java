package com.codeit.duckhu.domain.notification.repository;

import com.codeit.duckhu.domain.notification.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID>,NotificationRepositoryCustom {

  List<Notification> findAllByReceiverId(UUID receiverId);

  long countByReceiverId(UUID receiverId);

  void deleteOldConfirmedNotifications(Instant cutoff);

  void bulkMarkAsConfirmed(UUID receiverId, Instant now);

  List<Notification> findDescNoCursor(UUID receiverId, Pageable pageable);

  List<Notification> findDescWithCursor(UUID receiverId, Instant cursor, Pageable pageable);

  List<Notification> findAscNoCursor(UUID receiverId, Pageable pageable);

  List<Notification> findAscWithCursor(UUID receiverId, Instant cursor, Pageable pageable);
}