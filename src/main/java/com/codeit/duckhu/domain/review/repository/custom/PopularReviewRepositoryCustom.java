package com.codeit.duckhu.domain.review.repository.custom;

import com.codeit.duckhu.domain.review.entity.PopularReview;
import com.codeit.duckhu.global.type.Direction;
import com.codeit.duckhu.global.type.PeriodType;
import java.time.Instant;
import java.util.List;

public interface PopularReviewRepositoryCustom {
  List<PopularReview> findReviewsWithCursor(
      PeriodType period,
      Direction direction,
      String cursor,
      Instant after,
      int size);

  long countByPeriodSince(PeriodType period, Instant from);

  void deleteByPeriod(PeriodType period);
}
