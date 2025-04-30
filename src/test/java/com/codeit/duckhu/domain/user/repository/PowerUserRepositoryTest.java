package com.codeit.duckhu.domain.user.repository;

import com.codeit.duckhu.domain.review.repository.TestJpaConfig;
import com.codeit.duckhu.domain.user.dto.PowerUserStatsDto;
import com.codeit.duckhu.domain.user.entity.PowerUser;
import com.codeit.duckhu.domain.user.repository.poweruser.PowerUserRepository;
import com.codeit.duckhu.domain.user.repository.poweruser.PowerUserRepositoryImpl;
import com.codeit.duckhu.global.type.Direction;
import com.codeit.duckhu.global.type.PeriodType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Sql("/data.sql")
@Import({PowerUserRepositoryImpl.class, TestJpaConfig.class})
public class PowerUserRepositoryTest {

    @Autowired
    PowerUserRepository powerUserRepository;

    @Test
    @DisplayName("활동 점수 계산 성공")
    @Transactional
    void powerUserStats_success() {
        //given
        Instant now = Instant.now();
        Instant start = now.minus(1, ChronoUnit.DAYS);

        //when
        List<PowerUserStatsDto> stats = powerUserRepository.findPowerUserStatsBetween(start, now);

        //then
        assertThat(stats).isNotEmpty();
        assertThat(stats.get(0).reviewScoreSum()).isGreaterThan(0.0);
        assertThat(stats.get(0).likedCount()).isGreaterThan(0);
        assertThat(stats.get(0).commentCount()).isGreaterThan(0);

    }

    @Test
    @DisplayName("커서 없이 첫 페이지 ASC정렬 조회 성공")
    void searchPowerUserStats_asc() {
        //given
        //한달 전
        Instant after = Instant.now().minus(30,ChronoUnit.DAYS);
        int limit = 10;
        //when
        List<PowerUser> result = powerUserRepository.searchByPeriodWithCursorPaging(
                PeriodType.MONTHLY,
                Direction.ASC,
                null,
                after,
                limit
        );

        //then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSizeLessThanOrEqualTo(limit);
        assertThat(result).isSortedAccordingTo(Comparator.comparing(PowerUser::getScore));
    }

    @Test
    @DisplayName("커서 없이 첫 페이지 DESC정렬 조회 성공")
    void searchPowerUserStats_desc() {
        //given
        Instant after = Instant.now().minus(30, ChronoUnit.DAYS);
        int limit = 10;

        //when
        List<PowerUser> result = powerUserRepository.searchByPeriodWithCursorPaging(
                PeriodType.MONTHLY,
                Direction.DESC,
                null,
                after,
                limit
        );

        //then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSizeLessThanOrEqualTo(limit);
        assertThat(result).isSortedAccordingTo(Comparator.comparing(PowerUser::getScore).reversed());
    }

}
