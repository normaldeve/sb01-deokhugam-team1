package com.codeit.duckhu.domain.notification.exception;

import com.codeit.duckhu.global.exception.ErrorCode;

public class NotificationAlreadyConfirmedException extends NotificationException {
  public NotificationAlreadyConfirmedException(ErrorCode errorCode) {
    super(errorCode);
  }
}
