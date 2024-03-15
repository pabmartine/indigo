package com.martinia.indigo.notification.infrastructure.api.mappers;

import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.infrastructure.api.model.NotificationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationDtoMapper {

	@Mapping(target = "date", dateFormat = "dd/MM/yyyy HH:mm:ss")
	NotificationDto domain2Dto(Notification domain);

	@Mapping(target = "date", dateFormat = "dd/MM/yyyy HH:mm:ss")
	Notification dto2Domain(NotificationDto dto);

	@Mapping(target = "date", dateFormat = "dd/MM/yyyy HH:mm:ss")
	List<NotificationDto> domains2Dtos(List<Notification> domains);
}