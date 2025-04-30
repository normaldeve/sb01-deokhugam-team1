package com.codeit.duckhu.domain.book.repository.popular;

import com.codeit.duckhu.domain.book.entity.PopularBook;
import com.codeit.duckhu.global.type.Direction;
import com.codeit.duckhu.global.type.PeriodType;
import java.time.Instant;
import java.util.List;

public interface PopularBookRepositoryCustom {

  List<PopularBook> searchByPeriodWithCursorPaging(
      PeriodType period, Direction direction, String cursor, Instant after, int limit);
}
