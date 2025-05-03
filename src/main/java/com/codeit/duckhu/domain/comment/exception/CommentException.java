package com.codeit.duckhu.domain.comment.exception;

import com.codeit.duckhu.global.exception.DomainException;
import com.codeit.duckhu.global.exception.ErrorCode;

public class CommentException extends DomainException {
  public CommentException(ErrorCode errorCode) {
    super(errorCode);
  }
}
