package com.martinia.indigo.adapters.in.rest.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.martinia.indigo.adapters.in.rest.dtos.NotificationDto;
import com.martinia.indigo.domain.model.Notification;

@Mapper(componentModel = "spring")
public interface NotificationDtoMapper {

	NotificationDto domain2Dto(Notification domain);
	
	Notification dto2Domain(NotificationDto dto);

	List<NotificationDto> domains2Dtos(List<Notification> domains);
}