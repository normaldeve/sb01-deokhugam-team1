package com.codeit.duckhu.domain.notification.mapper;

import com.codeit.duckhu.domain.notification.dto.NotificationDto;
import com.codeit.duckhu.domain.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

  // notification에서 receiverid가 userid이다
  @Mapping(source = "receiverId", target = "userId")
  NotificationDto toDto(Notification notification);
}
