package com.codeit.duckhu.domain.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewUpdateRequest {

  private String content;

  @Min(value = 1, message = "평점은 1 이상이어야 합니다.")
  @Max(value = 5, message = "평점은 5 이하여야 합니다.")
  private Integer rating;

  private UUID bookId;
  private UUID userId;
}
