package com.codeit.duckhu.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Custom Error 지정 필요성이 있다면 에러 코드를 작성하여 진행.
 *
 * <p>일반적인 상황의 에러만 적혀있지만, 특정 도메인에서 발생할 수 있는 에러 등 정의\n\n
 * CustomException(ErrorCode.INTERNAL_SERVER_ERROR) 처럼 사용.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  // COMMON
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러입니다.", "관리자에게 연락해 주세요."),
  NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 자원을 찾을 수 없습니다.", "잘못된 요청을 진행하였습니다."),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다.", "잘못된 요청을 진행하였습니다."),
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다.", "잘못된 요청을 진행하였습니다."),

  // BOOK
  DUPLICATE_ISBN(HttpStatus.CONFLICT, "이미 존재하는 ISBN입니다.", "잘못된 요청을 진행하였습니다."),
  INVALID_ISBN_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 ISBN 형식입니다.", "잘못된 요청을 진행하였습니다."),
  BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "ISBN으로 책 정보를 찾을 수 없습니다.", "정보를 찾을 수 없습니다"),
  UNABLE_EXTRACT_ISBN(HttpStatus.BAD_REQUEST, "이미지에서 ISBN을 확인할 수 없습니다.", "잘못된 요청을 진행하였습니다."),
  INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 이미지 형식입니다.", "잘못된 요청을 진행하였습니다."),
  IMAGE_BASE64_CONVERSION_FAIL(
      HttpStatus.BAD_REQUEST,
      "이미지를 Base64로 변환하는 데 실패했습니다.",
      "잘못된 이미지 URL이거나 이미지 접근에 실패했습니다."
  ),
  NAVER_API_REQUEST_FAIL(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "네이버 도서 API 요청에 실패했습니다.",
      "외부 API 호출 중 오류가 발생했습니다."
  ),

  // OCR
  IMAGE_STREAM_READ_FAIL(HttpStatus.BAD_REQUEST, "이미지 스트림을 읽을 수 없습니다.",
      "이미지 파일이 손상되었거나 읽을 수 없습니다."),
  OCR_PROCESSING_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "OCR 처리에 실패했습니다.",
      "OCR 서비스 처리 중 문제가 발생했습니다."),
  GOOGLE_VISION_CLIENT_INIT_FAIL(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "Google Vision 클라이언트 생성에 실패했습니다.",
      "OCR 클라이언트를 초기화하는 중 문제가 발생했습니다."
  ),


  //S3
  S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 이미지 업로드에 실패했습니다.", "이미지 저장 중 오류 발생"),
  S3_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 객체 삭제에 실패했습니다.", "이미지를 삭제할 수 없습니다."),
  PRESIGNED_URL_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Presigned URL 생성 실패", "이미지 접근 URL 생성 중 오류 발생"),

  // USER
  EMAIL_DUPLICATION(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다.", "USER_409"),
  LOGIN_INPUT_INVALID(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.", "USER_401"),
  NOT_FOUND_USER(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다.", "USER_404"),
  UNAUTHORIZED_UPDATE(HttpStatus.FORBIDDEN, "사용자 정보 수정 권한 없음", "USER_403"),
  UNAUTHORIZED_USER(HttpStatus.FORBIDDEN, "사용자 권한 없음", "USER_403"),
  UNAUTHORIZED_DELETE(HttpStatus.FORBIDDEN, "사용자 삭제 권한 없음", "USER_403"),

  // notification
  NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 알림을 찾을 수 없습니다.", "존재하지 않는 알림입니다."),
  NOTIFICATION_ALREADY_CONFIRMED(HttpStatus.BAD_REQUEST, "이미 확인된 알림입니다.", "해당 알림은 이미 확인되었습니다."),
  FAILED_TO_CREATE_NOTIFICATION(
      HttpStatus.INTERNAL_SERVER_ERROR, "알림 생성에 실패했습니다.", "알림 저장 중 오류가 발생했습니다."),
  INVALID_NOTIFICATION_RECEIVER(HttpStatus.FORBIDDEN, "알림 수정 권한이 없습니다.", "해당 알림에 접근할 수 없습니다."),

  // comment
  NO_USER_IN_HEADER(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.", "요청 유저를 찾을 수 없습니다."),
  NO_AUTHORITY_USER(HttpStatus.FORBIDDEN, "잘못된 요청입니다.", "해당 유저에게 권한이 없습니다."),
  NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, "잘못된 요청입니다.", "해당하는 댓글을 찾을 수 없습니다."),

  // review
  REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다.", "리뷰를 찾을 수 없습니다."),
  REVIEW_ALREADY_EXISTS_BY_BOOK(HttpStatus.CONFLICT, "이미 작성한 리뷰가 존재합니다.", "한 도서에 리뷰는 1개만 작성할 수 있습니다."),
  REVIEW_IS_DELETED(HttpStatus.BAD_REQUEST, "삭제된 리뷰입니다.", "이미 삭제된 리뷰입니다."),

  // batch
  BATCH_PARAMETER_ERROR(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.", "기간 정보가 비어있습니다.");

  private final HttpStatus status;
  private final String message;
  private final String detail;
}
