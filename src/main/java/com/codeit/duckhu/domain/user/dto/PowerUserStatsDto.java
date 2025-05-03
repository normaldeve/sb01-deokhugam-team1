package com.codeit.duckhu.domain.user.dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public record PowerUserStatsDto(
    UUID userId, Double reviewScoreSum, Integer likedCount, Integer commentCount) {}
