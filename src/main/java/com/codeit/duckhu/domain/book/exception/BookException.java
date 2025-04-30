package com.codeit.duckhu.domain.book.exception;

import com.codeit.duckhu.global.exception.DomainException;
import com.codeit.duckhu.global.exception.ErrorCode;

public class BookException extends DomainException {

  public BookException(ErrorCode errorCode) {
    super(errorCode);
  }
}
