package com.codeit.duckhu.domain.user.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PowerUserDto {
  private UUID userId;
  private String nickname;
  private String period;
  private Instant createdAt;
  private Integer rank;
  private Double score;
  private Double reviewScoreSum;
  private Integer likeCount;
  private Integer commentCount;
}
