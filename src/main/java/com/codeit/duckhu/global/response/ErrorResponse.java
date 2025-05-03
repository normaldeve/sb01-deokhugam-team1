package com.codeit.duckhu.global.response;

import com.codeit.duckhu.domain.user.exception.EmailDuplicateException;
import com.codeit.duckhu.global.exception.DomainException;
import com.codeit.duckhu.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;

/**
 * 에러 응답 시 사용하는 응답 record.
 *
 * @param timestamp 에러 발생 시간
 * @param status 상태 //@param message 메시지 (어떤 에러가 발생했는지) //@param details 해당 에러에 대한 세부 사항
 */
@Builder
public record ErrorResponse(
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            timezone = "UTC")
        Instant timestamp,
    String code,
    String message,
    Map<String, String> details,
    String exceptionType,
    int status) {

  public static ErrorResponse of(DomainException e) {
    ErrorCode errorCode = e.getErrorCode();
    Map<String, String> details = new HashMap<>();
    if (e instanceof EmailDuplicateException emailEx) {
      details = Map.of("email", emailEx.getEmail());
    }

    return ErrorResponse.builder()
        .timestamp(Instant.now())
        .code(errorCode.name())
        .message(errorCode.getMessage())
        .details(details)
        .exceptionType(e.getClass().getSuperclass().getSimpleName())
        .status(errorCode.getStatus().value())
        .build();
  }
}
