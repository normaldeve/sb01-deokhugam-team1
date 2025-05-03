package com.codeit.duckhu.domain.notification.exception;

import com.codeit.duckhu.global.exception.ErrorCode;

public class NotificationNotFoundException extends NotificationException {

  public NotificationNotFoundException(ErrorCode errorCode) {
    super(errorCode);
  }
}
