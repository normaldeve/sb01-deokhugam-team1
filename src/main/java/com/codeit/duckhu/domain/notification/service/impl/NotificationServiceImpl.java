package com.codeit.duckhu.domain.notification.service.impl;

import com.codeit.duckhu.domain.comment.repository.CommentRepository;
import com.codeit.duckhu.domain.notification.dto.CursorPageResponseNotificationDto;
import com.codeit.duckhu.domain.notification.dto.NotificationDto;
import com.codeit.duckhu.domain.notification.entity.Notification;
import com.codeit.duckhu.domain.notification.exception.NotificationAccessDeniedException;
import com.codeit.duckhu.domain.notification.exception.NotificationAlreadyConfirmedException;
import com.codeit.duckhu.domain.notification.exception.NotificationNotFoundException;
import com.codeit.duckhu.domain.notification.mapper.NotificationMapper;
import com.codeit.duckhu.domain.notification.repository.NotificationRepository;
import com.codeit.duckhu.domain.notification.service.NotificationService;
import com.codeit.duckhu.domain.review.entity.Review;
import com.codeit.duckhu.domain.review.repository.ReviewRepository;
import com.codeit.duckhu.domain.user.entity.User;
import com.codeit.duckhu.domain.user.exception.UserException;
import com.codeit.duckhu.domain.user.repository.UserRepository;
import com.codeit.duckhu.global.exception.DomainException;
import com.codeit.duckhu.global.exception.ErrorCode;
import com.codeit.duckhu.global.type.PeriodType;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;
  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;
  private final CommentRepository commentRepository;
  private final MeterRegistry meterRegistry;

  /**
   * 내가 작성한 리뷰에 다른 사용자가 좋아요를 누르면 알림을 생성한다.
   *
   * @param reviewId 알림 대상 리뷰 ID
   * @param triggerUserId 좋아요를 누른 사용자 ID
   * @return 생성된 알림 객체
   */
  @Override
  @Transactional
  public NotificationDto createNotifyByLike(UUID reviewId, UUID triggerUserId) {

    log.debug("createNotifyByLike.start reviewId={} triggerUserId={}", reviewId, triggerUserId);

    // 1. 리뷰 조회 → 수신자 ID 확보
    Review review =
        reviewRepository
            .findById(reviewId)
            .orElseThrow(
                () -> {
                  log.warn("리뷰 없음: reviewId={}", reviewId);
                  return new NoSuchElementException("리뷰가 존재하지 않습니다.");
                });

    // 2. 수신자 ID = 리뷰 작성자의 ID
    UUID receiverId = review.getUser().getId();

    // 3. 트리거 유저 정보 → 닉네임 조회
    User triggerUser =
        userRepository
            .findById(triggerUserId)
            .orElseThrow(
                () -> {
                  log.warn("사용자 없음: triggerUserId={}", triggerUserId);
                  return new UserException(ErrorCode.NOT_FOUND_USER);
                });
    String nickname = triggerUser.getNickname();

    // Todo
    // 현재는 자기 자신에게 좋아요/댓글을 남겨도 알림이 생성되도록 설계되어 있음.
    // 향후 비즈니스 정책 변경 시, triggerUserId == receiverId 조건으로 필터링 필요

    // 4. 알림 생성
    Notification notification =
        Notification.forLike(reviewId, receiverId, nickname, review.getContent());
    Notification saved = notificationRepository.save(notification);
    log.info("createNotifyByLike.success notificationId={}", saved.getId());

    // DTO 생성
    return notificationMapper.toDto(saved);
  }

  /**
   * 내가 작성한 리뷰에 다른 사용자가 댓글을 남기면 알림을 생성한다.
   *
   * @param reviewId 알림 대상 리뷰 ID
   * @param triggerUserId 댓글을 작성한 사용자 ID
   * @param comment 댓글 내용
   * @return 생성된 알림 객체
   */
  @Override
  @Transactional
  public NotificationDto createNotifyByComment(UUID reviewId, UUID triggerUserId, String comment) {
    log.info("댓글 알림 생성 시작: reviewId={}, triggerUserId={}", reviewId, triggerUserId);

    // 1. 리뷰 조회 → 알림 수신자 확인 + 리뷰 제목 확보
    Review review =
        reviewRepository
            .findById(reviewId)
            .orElseThrow(
                () -> {
                  log.warn("리뷰 없음: reviewId={}", reviewId);
                  return new DomainException(ErrorCode.REVIEW_NOT_FOUND);
                });

    // 2. 수신자 ID = 리뷰 작성자의 ID
    UUID receiverId = review.getUser().getId();

    // 3. 댓글 작성자 정보 -> 닉네임
    User triggerUser =
        userRepository
            .findById(triggerUserId)
            .orElseThrow(
                () -> {
                  log.warn("사용자 없음: triggerUserId={}", triggerUserId);
                  return new UserException(ErrorCode.NOT_FOUND_USER);
                });
    String nickname = triggerUser.getNickname();

    // 알림 객체 생성
    Notification notification =
        Notification.forComment(reviewId, receiverId, nickname, comment, review.getContent());
    Notification saved = notificationRepository.save(notification);
    log.info("댓글 알림 생성 완료: notificationId={}", saved.getId());

    return notificationMapper.toDto(saved);
  }

  /**
   * 사용자가 기간 별 인기 리뷰 10위에 진입하면 알림 생성
   *
   * @param reviewId 알림 대상 리뷰 ID
   * @param receiverId 알림 대상 사용자 ID
   * @param period 인기 리뷰의 특정 기간
   * @param rank 인기 리뷰의 랭킹
   * @return 생성된 알림 객체
   */
  @Override
  @Transactional
  public NotificationDto createNotifyByPopularReview(
      UUID reviewId, UUID receiverId, PeriodType period, int rank) {
    log.info(
        "인기 리뷰 알림 생성 시작: reviewId={}, receiverId={}, period={}, rank={}",
        reviewId,
        receiverId,
        period,
        rank);

    // 1) 리뷰 조회 → reviewTitle 확보
    Review review =
        reviewRepository
            .findById(reviewId)
            .orElseThrow(
                () -> {
                  log.warn("리뷰 없음: reviewId={}", reviewId);
                  return new DomainException(ErrorCode.REVIEW_NOT_FOUND);
                });
    String reviewTitle = review.getContent();

    // 2) 알림 생성
    Notification notification =
        Notification.forPopularReview(reviewId, receiverId, period, rank, reviewTitle);
    Notification saved = notificationRepository.save(notification);

    log.info("인기 리뷰 알림 생성 완료: notificationId={}", saved.getId());
    return notificationMapper.toDto(saved);
  }

  /**
   * 알림 목록 조회
   *
   * @param receiverId 알림 대상 사용자 ID
   * @param direction 목록 정렬 방향
   * @param cursor 이전에 봤던 마지막 알림
   * @param limit 알림 목록 최대 개수
   * @return 생성된 알림 목록
   */
  @Override
  public CursorPageResponseNotificationDto getNotifications(
      UUID receiverId, String direction, Instant cursor, int limit) {
    log.debug("getNotifications.start receiverId={} dir={} cursor={} limit={}", receiverId, direction, cursor, limit);
    // 1) 정렬 방향 & 페이징 설정
    Sort.Direction sortDir =
        "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
    Pageable pageable = PageRequest.of(0, limit + 1, Sort.by(sortDir, "createdAt"));

    // 2) JPQL 메서드 분기 호출
    List<Notification> raw;
    if ("ASC".equalsIgnoreCase(direction)) {
      raw =
          (cursor == null)
              ? notificationRepository.findAscNoCursor(receiverId, pageable)
              : notificationRepository.findAscWithCursor(receiverId, cursor, pageable);
    } else {
      raw =
          (cursor == null)
              ? notificationRepository.findDescNoCursor(receiverId, pageable)
              : notificationRepository.findDescWithCursor(receiverId, cursor, pageable);
    }

    // 3) hasNext 판단 및 잘라내기
    boolean hasNext = raw.size() > limit;
    List<Notification> page = hasNext ? raw.subList(0, limit) : raw;

    // 4) DTO 변환
    List<NotificationDto> content =
        page.stream().map(notificationMapper::toDto).collect(Collectors.toList());

    // 5) nextCursor/nextAfter
    Instant nextAfter = hasNext ? page.get(page.size() - 1).getCreatedAt() : null;
    String nextCursor = nextAfter != null ? nextAfter.toString() : null;

    // 6) 전체 카운트
    long total = notificationRepository.countByReceiverId(receiverId);
    log.info("전체 알림 카운트: receiverId={}, total={}", receiverId, total);

    return new CursorPageResponseNotificationDto(
        content, nextCursor, nextAfter, content.size(), total, hasNext);
  }

  /**
   * 알림의 확인(읽음) 상태를 업데이트합니다.
   *
   * @param notificationId 읽음 상태를 변경할 알림의 ID
   * @param receiverId 현재 요청을 보낸 사용자(알림 수신자)의 ID
   * @param confirmed 읽음 상태(true면 확인 처리)
   * @return 읽음 처리된 알림의 DTO 정보
   * @throws NotificationNotFoundException 알림이 존재하지 않는 경우
   * @throws NotificationAccessDeniedException 알림의 수신자와 요청자가 일치하지 않는 경우
   */
  @Override
  @Transactional
  public NotificationDto updateConfirmedStatus(
      UUID notificationId, UUID receiverId, boolean confirmed) {
    // 1. 알림 ID로 알림을 조회한다. 없으면 404 예외 발생
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(
                () -> {
                  log.warn("알림을 찾을 수 없음: notificationId={}", notificationId);
                  return new NotificationNotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND);
                });

    // 2. 알림의 수신자가 현재 요청자와 다를 경우 접근 권한 없음 예외 발생
    if (!notification.getReceiverId().equals(receiverId)) {
      log.warn(
          "알림 접근 권한 오류: notificationId={}, 요청자={}, 실제 수신자={}",
          notificationId,
          receiverId,
          notification.getReceiverId());
      throw new NotificationAccessDeniedException(ErrorCode.INVALID_NOTIFICATION_RECEIVER);
    }

    // 이미 확인된 알림에 대해 다시 true 요청이 들어오면 400
    if (confirmed && notification.isConfirmed()) {
      log.debug("이미 읽음 처리된 알림에 중복 요청: notificationId={}", notificationId);
      throw new NotificationAlreadyConfirmedException(ErrorCode.NOTIFICATION_ALREADY_CONFIRMED);
    }

    // 3. 요청값이 true인 경우에만 확인 처리 (추후 false 처리 허용 시 확장 가능)
    // updated_at이 수정되지 않아 삭제가 진행되지 않아 save를 통한 실제 update쿼리를 날린다.
    if (confirmed) {
      notification.markAsConfirmed();
      notificationRepository.save(notification);
    }

    // 4. 업데이트된 알림 정보를 DTO로 변환하여 반환
    return notificationMapper.toDto(notification);
  }

  /**
   * 사용자의 모든 알림을 읽음 상태로 일괄 처리합니다.
   *
   * @param receiverId 알림을 읽음 처리할 사용자 ID
   */
  @Override
  @Transactional
  public void updateAllConfirmedStatus(UUID receiverId) {
    // 데이터를 불러오지 않고 DB에 직접 해당 사용자의 알림을 전부 confirmed로 update시킨다.
    log.info("전체 알림 읽음 처리 시작 (JPQL): receiverId={}", receiverId);
    Instant now = Instant.now();
    notificationRepository.bulkMarkAsConfirmed(receiverId, now);
    log.info("읽음 처리 완료 시각: {}", now);
  }

  /**
   * 매일 00시 30분에 1주일 지난 confirmed 알림을 삭제
   */
  @Override
  @Transactional
  @Scheduled(cron = "0 30 0 * * *", zone = "Asia/Seoul")
  public void deleteConfirmedNotificationsOlderThanAWeek() {
    try{
      Instant cutoff = Instant.now().minus(7, ChronoUnit.DAYS);
      log.info("Deleting confirmed notifications before {}", cutoff);
      notificationRepository.deleteOldConfirmedNotifications(cutoff);

      meterRegistry.counter("batch.notification.delete.success").increment();
    } catch (Exception e){
      meterRegistry.counter("batch.notification.delete.failure").increment();
    }

  }
}
