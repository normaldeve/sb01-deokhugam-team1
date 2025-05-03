package com.codeit.duckhu.domain.notification.controller.api;

import com.codeit.duckhu.domain.notification.dto.CursorPageResponseNotificationDto;
import com.codeit.duckhu.domain.notification.dto.NotificationDto;
import com.codeit.duckhu.domain.notification.dto.NotificationUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "알림 관리", description = "알림 관련 API")
@RequestMapping("/api/notifications")
public interface NotificationApi {

  @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 조회합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "알림 목록 조회 성공",
        content =
            @Content(schema = @Schema(implementation = CursorPageResponseNotificationDto.class))),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 (정렬 방향 오류, 페이지네이션 파라미터 오류, 사용자 ID 누락)",
        content =
            @Content(schema = @Schema(implementation = CursorPageResponseNotificationDto.class))),
    @ApiResponse(
        responseCode = "404",
        description = "사용자 정보 없음",
        content =
            @Content(schema = @Schema(implementation = CursorPageResponseNotificationDto.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content =
            @Content(schema = @Schema(implementation = CursorPageResponseNotificationDto.class)))
  })
  @GetMapping
  ResponseEntity<CursorPageResponseNotificationDto> getNotifications(
      @Parameter(
              name = "userId",
              description = "사용자 ID",
              example = "123e4567-e89b-12d3-a456-426614174000",
              required = true,
              in = ParameterIn.QUERY)
          @RequestParam
          UUID userId,
      @Parameter(
              name = "direction",
              description = "정렬 방향 (ASC | DESC)",
              example = "DESC",
              in = ParameterIn.QUERY)
          @RequestParam(defaultValue = "DESC")
          String direction,
      @Parameter(
              name = "cursor",
              description = "커서 페이지네이션 커서",
              example = "2025-04-28T06:53:17.683Z",
              in = ParameterIn.QUERY)
          @RequestParam(required = false)
          Instant cursor,
      @Parameter(name = "limit", description = "페이지 크기", example = "20", in = ParameterIn.QUERY)
          @RequestParam(defaultValue = "20")
          int limit);

  @Operation(summary = "알림 읽음 상태 업데이트", description = "특정 알림의 읽음 상태를 업데이트합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "알림 상태 업데이트 성공",
        content = @Content(schema = @Schema(implementation = NotificationDto.class))),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 (입력값 검증 실패, 요청자 ID 누락)",
        content = @Content(schema = @Schema(implementation = NotificationDto.class))),
    @ApiResponse(
        responseCode = "403",
        description = "알림 수정 권한 없음",
        content = @Content(schema = @Schema(implementation = NotificationDto.class))),
    @ApiResponse(
        responseCode = "404",
        description = "알림 정보 없음",
        content = @Content(schema = @Schema(implementation = NotificationDto.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content = @Content(schema = @Schema(implementation = NotificationDto.class)))
  })
  @PatchMapping("/{notificationId}")
  ResponseEntity<NotificationDto> updateConfirmedStatus(
      @Parameter(
              name = "notificationId",
              description = "알림 ID",
              example = "123e4567-e89b-12d3-a456-426614174000",
              required = true,
              in = ParameterIn.PATH)
          @PathVariable
          UUID notificationId,
      @Parameter(
              name = "Deokhugam-Request-User-ID",
              description = "요청자 ID",
              in = ParameterIn.HEADER,
              required = true,
              example = "123e4567-e89b-12d3-a456-426614174000")
          @RequestHeader("Deokhugam-Request-User-ID")
          UUID userId,
      @RequestBody(
              description = "읽음 여부 정보",
              required = true,
              content =
                  @Content(schema = @Schema(implementation = NotificationUpdateRequest.class)))
          NotificationUpdateRequest request);

  @Operation(summary = "모든 알림 읽음 처리", description = "사용자의 모든 알림을 읽음 상태로 일괄 처리합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "알림 읽음 처리 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (사용자 ID 누락)"),
    @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PatchMapping("/read-all")
  ResponseEntity<Void> updateAllConfirmedStatus(
      @Parameter(
              name = "Deokhugam-Request-User-ID",
              description = "요청자 ID",
              in = ParameterIn.HEADER,
              required = true,
              example = "123e4567-e89b-12d3-a456-426614174000")
          @RequestHeader("Deokhugam-Request-User-ID")
          UUID userId);
}
