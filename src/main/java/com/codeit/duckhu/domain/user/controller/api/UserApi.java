package com.codeit.duckhu.domain.user.controller.api;

import com.codeit.duckhu.domain.user.dto.CursorPageResponsePowerUserDto;
import com.codeit.duckhu.domain.user.dto.UserDto;
import com.codeit.duckhu.domain.user.dto.UserLoginRequest;
import com.codeit.duckhu.domain.user.dto.UserRegisterRequest;
import com.codeit.duckhu.domain.user.dto.UserUpdateRequest;
import com.codeit.duckhu.global.type.Direction;
import com.codeit.duckhu.global.type.PeriodType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "사용자 관리", description = "사용자 관련 API")
public interface UserApi {
  @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "등록성공",
        content = @Content(schema = @Schema(implementation = UserDto.class))),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 또는 중복된 이메일",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "이메일 중복",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping(value = "/api/users/")
  ResponseEntity<UserDto> create(
      HttpServletRequest request, @RequestBody UserRegisterRequest userRegisterRequest);

  @Operation(summary = "로그인", description = "사용자 로그인을 처리합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "로그인 성공",
        content = @Content(schema = @Schema(implementation = UserDto.class))),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "로그인 실패(이메일 또는 비밀번호 불일치)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/api/users/login")
  ResponseEntity<UserDto> login(
      HttpServletRequest request, @RequestBody UserLoginRequest userLoginRequest);

  @Operation(summary = "사용자 정보 조회", description = "사용자 ID로 상세 정보를 조회합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "사용자 정보 조회 성공",
        content = @Content(schema = @Schema(implementation = UserDto.class))),
    @ApiResponse(
        responseCode = "404",
        description = "사용자 정보 없음",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/api/users/{userId}")
  ResponseEntity<UserDto> findById(@Parameter(description = "사용자 ID") @PathVariable UUID userId);

  @Operation(summary = "사용자 정보 수정", description = "사용자의 닉네임을 수정합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "사용자 정보 수정 성공",
        content = @Content(schema = @Schema(implementation = UserDto.class))),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 (입력값 검증 실패)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "사용자 정보 수정 권한 없음",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "사용자 정보 없음",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PatchMapping("/api/users/{userId}")
  ResponseEntity<UserDto> update(
      HttpServletRequest request,
      @Parameter(description = "사용자 ID") @PathVariable("userId") UUID targetId,
      @RequestBody @Valid UserUpdateRequest userUpdateRequest);

  @Operation(summary = "사용자 논리 삭제", description = "사용자를 논리적으로 삭제합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "사용자 삭제 성공"),
    @ApiResponse(
        responseCode = "403",
        description = "사용자 삭제 권한 없음",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "사용자 정보 없음",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @DeleteMapping("/api/users/{userId}")
  ResponseEntity<Void> softDelete(
      HttpServletRequest request,
      @Parameter(description = "사용자 ID") @PathVariable("userId") UUID targetId);

  @Operation(summary = "사용자 물리 삭제", description = "사용자를 물리적으로 삭제합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "사용자 삭제 성공"),
    @ApiResponse(
        responseCode = "403",
        description = "사용자 삭제 권한 없음",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "사용자 정보 없음",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @DeleteMapping("/api/users/{userId}/hard")
  ResponseEntity<Void> hardDelete(
      HttpServletRequest request,
      @Parameter(description = "사용자 ID") @PathVariable("userId") UUID targetId);

  @Operation(summary = "파워 유저 목록 조회", description = "기간별 파워 유저 목록을 조회합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "파워 유저 목록 조회 성공",
        content =
            @Content(schema = @Schema(implementation = CursorPageResponsePowerUserDto.class))),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청(랭킹 기간 오류, 정렬 방향 오류 등)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/api/users/power")
  ResponseEntity<CursorPageResponsePowerUserDto> findPowerUsers(
      @Parameter(description = "랭킹 기간") @RequestParam PeriodType period,
      @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "ASC") Direction direction,
      @Parameter(description = "커서 페이지네이션 커서") @RequestParam(required = false) String cursor,
      @Parameter(description = "보조 커서(createdAt)")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant after,
      @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "50") int limit);
}
