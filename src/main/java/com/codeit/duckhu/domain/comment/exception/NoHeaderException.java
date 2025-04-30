package com.codeit.duckhu.domain.comment.exception;

import com.codeit.duckhu.global.exception.ErrorCode;

public class NoHeaderException extends CommentException {
  public NoHeaderException(ErrorCode errorCode) {
    super(errorCode);
  }
}
