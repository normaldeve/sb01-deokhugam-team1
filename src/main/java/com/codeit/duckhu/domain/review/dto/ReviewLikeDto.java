package com.codeit.duckhu.domain.review.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewLikeDto {
  private UUID reviewId;
  private UUID userId;
  private boolean liked;
}
