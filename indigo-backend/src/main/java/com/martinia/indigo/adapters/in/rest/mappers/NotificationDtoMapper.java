package com.martinia.indigo.adapters.in.rest.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.martinia.indigo.adapters.in.rest.dtos.NotificationDto;
import com.martinia.indigo.domain.model.Notification;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationDtoMapper {

	@Mapping(target = "sendDate", dateFormat = "dd/MM/yyyy HH:mm:ss")
	NotificationDto domain2Dto(Notification domain);

	@Mapping(target = "sendDate", dateFormat = "dd/MM/yyyy HH:mm:ss")
	Notification dto2Domain(NotificationDto dto);

	@Mapping(target = "sendDate", dateFormat = "dd/MM/yyyy HH:mm:ss")
	List<NotificationDto> domains2Dtos(List<Notification> domains);
}