package com.codeit.duckhu.domain.review.dto;

import com.codeit.duckhu.global.type.PeriodType;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PopularReviewDto {

  private UUID id;

  private UUID reviewId;

  private UUID bookId;

  private String bookTitle;

  private String bookThumbnailUrl;

  private UUID userId;

  private String userNickname;

  private String reviewContent;

  private Double reviewRating;

  private PeriodType period;

  private Instant createdAt;

  private Integer rank;

  private Double score;

  private Integer likeCount;

  private Integer commentCount;
}
