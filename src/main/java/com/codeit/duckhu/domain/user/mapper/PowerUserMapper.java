package com.codeit.duckhu.domain.user.mapper;

import com.codeit.duckhu.domain.user.dto.PowerUserDto;
import com.codeit.duckhu.domain.user.entity.PowerUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PowerUserMapper {
  @Mapping(source = "user.id", target = "userId")
  @Mapping(source = "user.nickname", target = "nickname")
  PowerUserDto toDto(PowerUser powerUser);
}
