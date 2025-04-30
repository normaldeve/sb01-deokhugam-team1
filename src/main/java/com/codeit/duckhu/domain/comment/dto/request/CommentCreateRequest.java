package com.codeit.duckhu.domain.comment.dto.request;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentCreateRequest {
  private UUID userId;
  private UUID reviewId;
  private String content;
}
