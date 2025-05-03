package com.codeit.duckhu.domain.comment.exception;

import com.codeit.duckhu.global.exception.ErrorCode;

public class NoAuthorityException extends CommentException {

  public NoAuthorityException(ErrorCode errorCode) {
    super(errorCode);
  }
}
