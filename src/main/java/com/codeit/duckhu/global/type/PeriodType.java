package com.codeit.duckhu.global.type;

import java.time.Instant;

public enum PeriodType {
  DAILY,
  WEEKLY,
  MONTHLY,
  ALL_TIME;

  public Instant toStartInstant(Instant now) {
    return switch (this) {
      case DAILY -> now.minusSeconds(60 * 60 * 24); // 1일 전
      case WEEKLY -> now.minusSeconds(60 * 60 * 24 * 7); // 1주일 전
      case MONTHLY -> now.minusSeconds(60L * 60 * 24 * 30); // 30일 전
      case ALL_TIME -> Instant.EPOCH; // 올타임
    };
  }
}
