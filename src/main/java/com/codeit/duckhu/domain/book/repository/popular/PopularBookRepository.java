package com.codeit.duckhu.domain.book.repository.popular;

import com.codeit.duckhu.domain.book.entity.PopularBook;
import com.codeit.duckhu.global.type.PeriodType;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularBookRepository
    extends JpaRepository<PopularBook, UUID>, PopularBookRepositoryCustom {

  int countByPeriod(PeriodType period);

  void deleteByPeriod(PeriodType period);
}
