package com.codeit.duckhu.domain.book.exception;

import com.codeit.duckhu.global.exception.DomainException;
import com.codeit.duckhu.global.exception.ErrorCode;

public class CustomS3Exception extends DomainException {

  public CustomS3Exception(ErrorCode errorCode) {
    super(errorCode);
  }
}
