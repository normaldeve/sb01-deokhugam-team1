package com.codeit.duckhu.domain.user.repository.poweruser;

import com.codeit.duckhu.domain.user.entity.PowerUser;
import com.codeit.duckhu.global.type.PeriodType;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface PowerUserRepository
    extends JpaRepository<PowerUser, UUID>, PowerUserRepositoryCustom {

  //벌크연산
  @Modifying(clearAutomatically = true)
  @Transactional
  void deleteByPeriod(PeriodType period);
}
