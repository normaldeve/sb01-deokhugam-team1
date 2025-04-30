package com.codeit.duckhu.domain.user.exception;

import com.codeit.duckhu.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class EmailDuplicateException extends UserException {
  private final String email;
  public EmailDuplicateException(String email) {
    super(ErrorCode.EMAIL_DUPLICATION);
    this.email = email;
  }
}
