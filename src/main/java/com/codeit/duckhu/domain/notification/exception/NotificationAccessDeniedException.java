package com.codeit.duckhu.domain.notification.exception;

import com.codeit.duckhu.global.exception.ErrorCode;

public class NotificationAccessDeniedException extends NotificationException {

  public NotificationAccessDeniedException(ErrorCode errorCode) {
    super(errorCode);
  }
}
