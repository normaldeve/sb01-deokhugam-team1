package com.codeit.duckhu.domain.review.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CursorPageResponsePopularReviewDto {

  private List<PopularReviewDto> content;
  private String nextCursor;
  private Instant nextAfter;
  private int size;
  private Long totalElements;
  private boolean hasNext;
}
