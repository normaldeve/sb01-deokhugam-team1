package com.codeit.duckhu.domain.user.service;

import com.codeit.duckhu.domain.user.dto.CursorPageResponsePowerUserDto;
import com.codeit.duckhu.domain.user.dto.UserDto;
import com.codeit.duckhu.domain.user.dto.UserLoginRequest;
import com.codeit.duckhu.domain.user.dto.UserRegisterRequest;
import com.codeit.duckhu.domain.user.dto.UserUpdateRequest;
import com.codeit.duckhu.global.type.Direction;
import com.codeit.duckhu.global.type.PeriodType;
import java.time.Instant;
import java.util.UUID;

public interface UserService {
  UserDto create(UserRegisterRequest userRegisterRequest);

  UserDto login(UserLoginRequest userLoginRequest);

  UserDto findById(UUID id);

  UserDto update(UUID id, UserUpdateRequest userUpdateRequest);

  void softDelete(UUID id);

  void hardDelete(UUID id);

  void savePowerUser(PeriodType period);

  CursorPageResponsePowerUserDto findPowerUsers(
      PeriodType period, Direction direction, String cursor, Instant after, int limit);
}
