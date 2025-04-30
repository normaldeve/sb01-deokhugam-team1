package com.codeit.duckhu.domain.notification.exception;

import com.codeit.duckhu.global.exception.DomainException;
import com.codeit.duckhu.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class NotificationException extends DomainException {

  public NotificationException(ErrorCode errorCode) {
    super(errorCode);
  }
}
