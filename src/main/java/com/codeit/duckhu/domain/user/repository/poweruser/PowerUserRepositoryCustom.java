package com.codeit.duckhu.domain.user.repository.poweruser;

import com.codeit.duckhu.domain.user.dto.PowerUserStatsDto;
import com.codeit.duckhu.domain.user.entity.PowerUser;
import com.codeit.duckhu.global.type.Direction;
import com.codeit.duckhu.global.type.PeriodType;
import java.time.Instant;
import java.util.List;

public interface PowerUserRepositoryCustom {
  List<PowerUser> searchByPeriodWithCursorPaging(
      PeriodType period, Direction direction, String cursor, Instant after, int limit);
  List<PowerUserStatsDto> findPowerUserStatsBetween(Instant start, Instant end);
}
