package com.codeit.duckhu.domain.comment.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CursorPageResponseCommentDto {
  List<CommentDto> content;
  String nextCursor;
  Instant nextAfter;
  Integer size;
  Long totalElements;
  Boolean hasNext;
}
