package com.codeit.duckhu.domain.book.exception;

import com.codeit.duckhu.global.exception.ErrorCode;

public class NaverAPIException extends BookException {

  public NaverAPIException(ErrorCode errorCode) {
    super(errorCode);
  }
}
