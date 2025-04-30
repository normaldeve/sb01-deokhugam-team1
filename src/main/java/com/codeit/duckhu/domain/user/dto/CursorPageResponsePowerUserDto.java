package com.codeit.duckhu.domain.user.dto;

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
public class CursorPageResponsePowerUserDto {
  private List<PowerUserDto> content;
  private String nextCursor;
  private Instant nextAfter;
  private int size;
  private int totalElements;
  private boolean hasNext;
}
