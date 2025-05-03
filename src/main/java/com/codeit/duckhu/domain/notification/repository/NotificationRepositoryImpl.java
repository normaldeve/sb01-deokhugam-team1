package com.codeit.duckhu.domain.notification.repository;

import com.codeit.duckhu.domain.notification.entity.Notification;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

  private final EntityManager em;

  public NotificationRepositoryImpl(EntityManager em) {
    this.em = em;
  }

  @Override
  @Transactional
  public void deleteOldConfirmedNotifications(Instant cutoff) {
    em.createQuery(
            """
        DELETE FROM Notification n
         WHERE n.confirmed = true
           AND n.updatedAt < :cutoff
      """)
        .setParameter("cutoff", cutoff)
        .executeUpdate();
  }

  @Override
  @Transactional
  public void bulkMarkAsConfirmed(UUID receiverId, Instant now) {
    em.createQuery(
            """
        UPDATE Notification n
           SET n.confirmed = true, n.updatedAt = :now
         WHERE n.receiverId = :receiverId
           AND n.confirmed = false
      """)
        .setParameter("receiverId", receiverId)
        .setParameter("now", now)
        .executeUpdate();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Notification> findDescNoCursor(UUID receiverId, Pageable pageable) {
    return em.createQuery(
            """
        SELECT n
          FROM Notification n
         WHERE n.receiverId = :receiverId
         ORDER BY n.createdAt DESC
      """,
            Notification.class)
        .setParameter("receiverId", receiverId)
        .setFirstResult((int) pageable.getOffset())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Notification> findDescWithCursor(UUID receiverId, Instant cursor, Pageable pageable) {
    return em.createQuery(
            """
        SELECT n
          FROM Notification n
         WHERE n.receiverId = :receiverId
           AND n.createdAt < :cursor
      """,
            Notification.class)
        .setParameter("receiverId", receiverId)
        .setParameter("cursor", cursor)
        .setFirstResult((int) pageable.getOffset())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Notification> findAscNoCursor(UUID receiverId, Pageable pageable) {
    return em.createQuery(
            """
        SELECT n
          FROM Notification n
         WHERE n.receiverId = :receiverId
         ORDER BY n.createdAt ASC
      """,
            Notification.class)
        .setParameter("receiverId", receiverId)
        .setFirstResult((int) pageable.getOffset())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Notification> findAscWithCursor(UUID receiverId, Instant cursor, Pageable pageable) {
    return em.createQuery(
            """
        SELECT n
          FROM Notification n
         WHERE n.receiverId = :receiverId
           AND n.createdAt > :cursor
         ORDER BY n.createdAt ASC
      """,
            Notification.class)
        .setParameter("receiverId", receiverId)
        .setParameter("cursor", cursor)
        .setFirstResult((int) pageable.getOffset())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
