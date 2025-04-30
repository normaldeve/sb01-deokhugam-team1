package com.codeit.duckhu.domain.comment.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentDto {
  private UUID id;
  private UUID reviewId;
  private UUID userId;
  private String userNickname;
  private String content;
  private Instant createdAt;
  private Instant updatedAt;
}
