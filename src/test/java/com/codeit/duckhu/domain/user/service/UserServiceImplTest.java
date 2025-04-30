package com.codeit.duckhu.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.codeit.duckhu.domain.user.dto.CursorPageResponsePowerUserDto;
import com.codeit.duckhu.domain.user.dto.PowerUserDto;
import com.codeit.duckhu.domain.user.dto.PowerUserStatsDto;
import com.codeit.duckhu.domain.user.dto.UserDto;
import com.codeit.duckhu.domain.user.dto.UserLoginRequest;
import com.codeit.duckhu.domain.user.dto.UserRegisterRequest;
import com.codeit.duckhu.domain.user.dto.UserUpdateRequest;
import com.codeit.duckhu.domain.user.entity.PowerUser;
import com.codeit.duckhu.domain.user.entity.User;
import com.codeit.duckhu.domain.user.exception.EmailDuplicateException;
import com.codeit.duckhu.domain.user.exception.UserException;
import com.codeit.duckhu.domain.user.mapper.PowerUserMapper;
import com.codeit.duckhu.domain.user.mapper.UserMapper;
import com.codeit.duckhu.domain.user.repository.UserRepository;
import com.codeit.duckhu.domain.user.repository.poweruser.PowerUserRepository;
import com.codeit.duckhu.global.type.Direction;
import com.codeit.duckhu.global.type.PeriodType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
  @Mock private UserRepository userRepository;
  @Mock private UserMapper userMapper;
  @Mock private PowerUserMapper powerUserMapper;
  @Mock private PowerUserRepository powerUserRepository;

  @InjectMocks private UserServiceImpl sut;

  @Nested
  @DisplayName("사용자 회원가입 테스트")
  class RegisterUserTest {

    @Test
    @DisplayName("회원가입 성공")
    void register_success() {
      // given
      UserRegisterRequest request =
          new UserRegisterRequest("testA@example.com", "testA", "testa1234!");
      User user = new User("testA@example.com", "testA", "testa1234!");
      UUID id = user.getId();
      UserDto dto = new UserDto(id, "testA@example.com", "testA", user.getCreatedAt());
      given(userRepository.existsByEmail("testA@example.com")).willReturn(false);
      given(userRepository.save(any(User.class))).willReturn(user);
      given(userMapper.toDto(any(User.class))).willReturn(dto);

      // when
      UserDto result = sut.create(request);

      // then
      assertThat(result.getEmail()).isEqualTo("testA@example.com");
      assertThat(result.getNickname()).isEqualTo("testA");
      verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 이메일")
    void register_fail() {
      // given
      UserRegisterRequest dto = new UserRegisterRequest("testA@example.com", "testA", "testa1234!");
      given(userRepository.existsByEmail("testA@example.com")).willReturn(true);

      // when
      // then
      assertThatThrownBy(() -> sut.create(dto)).isInstanceOf(EmailDuplicateException.class);
    }
  }

  @Nested
  @DisplayName("로그인 테스트")
  class LoginUserTest {

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
      // given
      UserLoginRequest request = new UserLoginRequest("testA@example.com", "testa1234!");
      User user = new User("testA@example.com", "testA", "testa1234!");
      UUID id = user.getId();

      given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
      given(userMapper.toDto(user))
          .willReturn(new UserDto(id, "testA@example.com", "testA", user.getCreatedAt()));

      // when
      UserDto result = sut.login(request);

      // then
      assertThat(result.getEmail()).isEqualTo("testA@example.com");
      assertThat(result.getNickname()).isEqualTo("testA");
      verify(userRepository, times(1)).findByEmail("testA@example.com");
    }

    @Test
    @DisplayName("로그인 실패- 일치하지 않는 비밀번호")
    void login_fail() {
      // given
      UserLoginRequest request = new UserLoginRequest("testA@example.com", "aaaa1234!");
      User user = new User("testA@example.com", "testA", "testa1234!");
      given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));

      // when
      // then
      assertThatThrownBy(() -> sut.login(request)).isInstanceOf(UserException.class);
      verify(userRepository, times(1)).findByEmail("testA@example.com");
    }
  }

  @Nested
  @DisplayName("사용자 상세조회 실패 테스트")
  class FindUserTest {
    @Test
    @DisplayName("사용자 상세 조회 실패 - isDeleted=true")
    void find_fail() {
      // given
      UUID id = UUID.randomUUID();
      User user = new User("testA@example.com", "testA", "testa1234!");
      user.softDelete();
      given(userRepository.findById(id)).willReturn(Optional.of(user));

      // when
      // then
      assertThatThrownBy(() -> sut.findById(id)).isInstanceOf(UserException.class);
      verify(userRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("사용자 상세 조회 실패 - 사용자 null")
    void find_fail_null() {
      // given
      UUID id = UUID.randomUUID();
      // when
      // then
      assertThatThrownBy(() -> sut.findById(id)).isInstanceOf(UserException.class);
      verify(userRepository, times(1)).findById(id);
    }
  }

  @Nested
  @DisplayName("사용자 수정 테스트")
  class UpdateUserTest {
    @Test
    @DisplayName("사용자 수정 성공")
    void update_success() {
      // given
      UUID id = UUID.randomUUID();
      User user = new User("testA@example.com", "testA", "testa1234!");
      given(userRepository.findById(id)).willReturn(Optional.of(user));

      UserUpdateRequest request = new UserUpdateRequest("updateName");
      given(userMapper.toDto(user))
          .willReturn(new UserDto(id, "testA@example.com", "updateName", user.getCreatedAt()));

      // when
      UserDto result = sut.update(id, request);

      // then
      assertThat(result.getEmail()).isEqualTo("testA@example.com");
      assertThat(user.getNickname()).isEqualTo("updateName");
      verify(userRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("사용자 수정 실패 - 존재하지 않는 사용자")
    void update_fail() {
      // given
      UUID id = UUID.randomUUID();
      given(userRepository.findById(id)).willReturn(Optional.empty());

      // when
      // then
      assertThatThrownBy(() -> sut.update(id, new UserUpdateRequest("updateName")))
          .isInstanceOf(UserException.class)
          .hasMessage("해당 유저가 존재하지 않습니다.");
    }
  }

  @Nested
  @DisplayName("사용자 논리 삭제 테스트")
  class SoftDeleteUserTest {
    @Test
    @DisplayName("논리 삭제 성공")
    void softDelete_success() {
      // given
      UUID id = UUID.randomUUID();
      User user = new User("testA@example.com", "testA", "testa1234!");
      given(userRepository.findById(id)).willReturn(Optional.of(user));

      // when
      sut.softDelete(id);
      // then
      assertThat(user.isDeleted()).isTrue();
      verify(userRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("논리 삭제 실패 - 존재하지 않는 사용자")
    void softDelete_fail() {
      // given
      UUID id = UUID.randomUUID();
      given(userRepository.findById(id)).willReturn(Optional.empty());
      // when
      // then
      assertThatThrownBy(() -> sut.softDelete(id)).isInstanceOf(UserException.class);
    }
  }

  @Nested
  @DisplayName("사용자 물리 삭제 테스트")
  class HardDeleteTest {
    @Test
    @DisplayName("물리 삭제 성공")
    void hardDelete_success() {
      // given
      User user = new User("testA@example.com", "testA", "testa1234!");
      UUID id = user.getId();
      given(userRepository.findById(id)).willReturn(Optional.of(user));

      // when
      sut.hardDelete(id);

      // then
      verify(userRepository, times(1)).findById(id);
      verify(userRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("물리 삭제 실패 - 존재하지 않는 사용자")
    void hardDelete_fail() {
      // given
      UUID id = UUID.randomUUID();
      given(userRepository.findById(id)).willReturn(Optional.empty());
      // when
      // then
      assertThatThrownBy(() -> sut.hardDelete(id)).isInstanceOf(UserException.class);
    }
  }

    @Nested
    @DisplayName("파워유저 테스트")
    class PowerUserTest {
      @Test
      @DisplayName("파워 유저 조회 성공")
      void powerUser_success() {
        // given
        PeriodType period = PeriodType.DAILY;
        Direction direction = Direction.ASC;
        String cursor = null;
        Instant after = null;
        int limit = 10;

        User mockUser = User.builder()
                .email("testA@example.com")
                .nickname("testA")
                .password("testA1!")
                .build();
        PowerUser entity = PowerUser.builder()
                .user(mockUser)
                .period(period)
                .reviewScoreSum(10.0)
                .likeCount(5)
                .commentCount(3)
                .score(8.1)
                .rank(1)
                .build();

        PowerUserDto dto = PowerUserDto.builder()
                .userId(mockUser.getId())
                .nickname(mockUser.getNickname())
                .reviewScoreSum(10.0)
                .likeCount(5)
                .commentCount(3)
                .score(8.1)
                .rank(1)
                .period(period.name())
                .build();
        given(powerUserRepository.searchByPeriodWithCursorPaging(period,direction,cursor,after,limit+1))
                .willReturn(List.of(entity));
        given(powerUserMapper.toDto(entity)).willReturn(dto);


        // when
        CursorPageResponsePowerUserDto result =
            sut.findPowerUsers(period, direction, cursor, after, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(dto);
      }

      @Test
      @DisplayName("파워 유저 저장 테스트 성공")
      void savePowerUser_success() {
        //given
        PeriodType period = PeriodType.DAILY;
        Instant now = Instant.now();
        Instant start = period.toStartInstant(now);
        Instant end=now;

        UUID userId = UUID.randomUUID();
        PowerUserStatsDto statsDto = PowerUserStatsDto.builder()
                .userId(userId)
                .reviewScoreSum(7.0)
                .likedCount(5)
                .commentCount(10)
                .build();

        List<PowerUserStatsDto> stats = List.of(statsDto);
        User mockUser = User.builder()
                .email("testA@example.com")
                .nickname("testA")
                .password("testA1!")
                .build();
        ReflectionTestUtils.setField(mockUser, "id", userId);

        given(powerUserRepository.findPowerUserStatsBetween(any(), any())).willReturn(stats);
        given(userRepository.findAllById(any())).willReturn(List.of(mockUser));

        //when
        sut.savePowerUser(period);
        //then
        verify(powerUserRepository,times(1)).deleteByPeriod(period);
        verify(powerUserRepository,times(1)).saveAll(any());

      }
    }
}
