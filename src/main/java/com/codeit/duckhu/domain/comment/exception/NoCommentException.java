package com.codeit.duckhu.domain.comment.exception;

import com.codeit.duckhu.global.exception.ErrorCode;

public class NoCommentException extends CommentException {
  public NoCommentException(ErrorCode errorCode) {
    super(errorCode);
  }
}
