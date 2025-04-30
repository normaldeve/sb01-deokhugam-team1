package com.codeit.duckhu.domain.user.mapper;

import com.codeit.duckhu.domain.user.dto.UserDto;
import com.codeit.duckhu.domain.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
  UserDto toDto(User user);
}
