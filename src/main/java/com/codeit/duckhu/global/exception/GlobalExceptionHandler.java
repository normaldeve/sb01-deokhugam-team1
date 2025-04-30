package com.codeit.duckhu.global.exception;

import com.codeit.duckhu.domain.comment.exception.NoHeaderException;
import com.codeit.duckhu.global.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** 전역 예외 핸들러 - 전역 예외 처리를 위함. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 존재하지 않는 요청에 대한 예외.
   *
   * @param ex
   * @return 500 INTERNAL SERVER ERROR 응답
   */
  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex) {
    log.warn("요청한 페이지 없음: {}", ex.getRequestURL());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse.of(new DomainException(ErrorCode.NOT_FOUND)));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex) {
    log.warn("지원하지 않는 HTTP Method: {}", ex.getMethod());
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(ErrorResponse.of(new DomainException(ErrorCode.METHOD_NOT_ALLOWED)));
  }

  /**
   * 요청값 검증 실패
   *
   * @param e
   * @return 400 INVALID_INPUT_VALUE 응답
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException e) {
    log.error("Validation failed: {}", e.getMessage());
    Map<String, String> details = new LinkedHashMap<>();
    for (FieldError error : e.getBindingResult().getFieldErrors()) {
      details.putIfAbsent(error.getField(), error.getDefaultMessage());
    }

    //ErrorResponse를 공통으로 쓰고 있기 떄문에 여기서 따로만들어주기
    ErrorResponse response = ErrorResponse.builder()
            .timestamp(Instant.now())
            .code(ErrorCode.INVALID_INPUT_VALUE.name())
            .message(ErrorCode.INVALID_INPUT_VALUE.getMessage())
            .details(details)
            .exceptionType(DomainException.class.getSimpleName())
            .status(ErrorCode.INVALID_INPUT_VALUE.getStatus().value())
            .build();

    return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus()) // 400
        .body(response);
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
    log.error("Missing request header: {}", ex.getHeaderName());

    DomainException customException = new NoHeaderException(ErrorCode.NO_USER_IN_HEADER);

    return ResponseEntity.status(customException.getErrorCode().getStatus())
        .body(ErrorResponse.of(customException));
  }

  /**
   * 커스텀 예외에 대한 처리.
   *
   * @param e
   * @return 해당 Error 코드에 대응하는 에러 응답
   */
  @ExceptionHandler(value = {DomainException.class})
  public ResponseEntity<ErrorResponse> handleCustomException(DomainException e) {
    log.error(
        "handleCustomException() in GlobalExceptionHandler throw CustomException : {}",
        e.getMessage());
    return ResponseEntity.status(e.getErrorCode().getStatus()) // ex. 403, 409, 401
        .body(ErrorResponse.of(e));
  }

  /**
   * 기본적인 예외에 대한 처리
   *
   * @param e
   * @return 500 INTERNAL SERVER ERROR 응답
   */
  @ExceptionHandler(value = {Exception.class})
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("handleException() in GlobalExceptionHandler throw Exception : {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.of(new DomainException(ErrorCode.INTERNAL_SERVER_ERROR)));
  }
}
