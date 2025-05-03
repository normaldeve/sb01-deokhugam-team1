package com.codeit.duckhu.domain.notification.controller;

import com.codeit.duckhu.domain.notification.dto.CursorPageResponseNotificationDto;
import com.codeit.duckhu.domain.notification.dto.NotificationDto;
import com.codeit.duckhu.domain.notification.dto.NotificationUpdateRequest;
import com.codeit.duckhu.domain.notification.service.NotificationService;
import com.codeit.duckhu.domain.user.entity.User;
import com.codeit.duckhu.domain.user.exception.UserException;
import com.codeit.duckhu.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public CursorPageResponseNotificationDto getNotifications(
      HttpServletRequest httpservlet,
      @RequestParam UUID userId,
      @RequestParam(defaultValue = "DESC") String direction,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant cursor,
      @RequestParam(defaultValue = "20") int limit) {

    User authenticatedUser = (User) httpservlet.getAttribute("authenticatedUser");
    if (authenticatedUser == null) { // 로그인 하지 않은 사용자가 들어왔을때
      log.warn("비인증 사용자 알림 목록 요청 차단");
      throw new UserException(ErrorCode.UNAUTHORIZED_USER);
    }

    log.info(
        "API 호출: GET /api/notifications, userId={}, direction={}, cursor={}, limit={}",
        userId,
        direction,
        cursor,
        limit);

    CursorPageResponseNotificationDto dto =
        notificationService.getNotifications(userId, direction, cursor, limit);

    log.info(
        "API 응답 준비 완료: userId={}, returnedCount={}, hasNext={}",
        userId,
        dto.content().size(),
        dto.hasNext());

    return dto;
  }

  /**
   * 특정 알림의 읽음 상태를 업데이트합니다.
   *
   * @param notificationId 읽음 처리할 알림 ID
   * @param httpservlet 요청자 ID (헤더)
   * @param request 읽음 여부 정보
   * @return 업데이트된 알림 정보
   */
  @PatchMapping("/{notificationId}")
  public ResponseEntity<NotificationDto> updateConfirmedStatus(
      @PathVariable UUID notificationId,
      HttpServletRequest httpservlet,
      @RequestBody @Valid NotificationUpdateRequest request) {

    User authenticatedUser = (User) httpservlet.getAttribute("authenticatedUser");
    if (authenticatedUser == null) { // 로그인 하지 않은 사용자가 들어왔을때
      log.warn("비인증 사용자 알림 읽음 처리 시도 차단");
      throw new UserException(ErrorCode.UNAUTHORIZED_USER);
    }

    log.info(
        "알림 읽음 요청: notificationId={}, userId={}, confirmed={}",
        notificationId,
        authenticatedUser.getId(),
        request.confirmed());
    NotificationDto updated =
        notificationService.updateConfirmedStatus(
            notificationId, authenticatedUser.getId(), request.confirmed());
    log.info("알림 읽음 처리 완료: notificationId={}, userId={}", updated.id(), authenticatedUser.getId());
    return ResponseEntity.ok(updated);
  }

  /**
   * 사용자의 모든 알림을 읽음 상태로 일괄 처리합니다.
   *
   * @param httpServlet 요청자 ID (헤더)
   * @return 204 No Content
   */
  @PatchMapping("/read-all")
  public ResponseEntity<Void> updateAllConfirmedStatus(HttpServletRequest httpServlet) {
    User authenticatedUser = (User) httpServlet.getAttribute("authenticatedUser");
    if (authenticatedUser == null) { // 로그인 하지 않은 사용자가 들어왔을때
      log.warn("비인증 사용자 전체 알림 읽음 시도 차단");
      throw new UserException(ErrorCode.UNAUTHORIZED_USER);
    }

    log.info("전체 알림 읽음 요청: userId={}", authenticatedUser.getId());
    notificationService.updateAllConfirmedStatus(authenticatedUser.getId());
    log.info("전체 알림 읽음 처리 완료: userId={}", authenticatedUser.getId());

    return ResponseEntity.noContent().build();
  }
}
