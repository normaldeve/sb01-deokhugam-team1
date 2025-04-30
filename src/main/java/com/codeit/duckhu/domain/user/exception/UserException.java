package com.codeit.duckhu.domain.user.exception;

import com.codeit.duckhu.global.exception.DomainException;
import com.codeit.duckhu.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class UserException extends DomainException {

  public UserException(ErrorCode errorCode) {
    super(errorCode);
  }
}
