package com.martinia.indigo.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.martinia.indigo.dto.NotificationDto;
import com.martinia.indigo.model.indigo.Notification;

/**
 * Mapping interface from Notification to NotificationDTO
 *
 */
@Mapper(componentModel = "spring")
public interface NotificationDtoMapper {

	/**
	 * Transforms a notification object into a DTO
	 * 
	 * @param price domain object
	 * @return dto
	 */

	NotificationDto notificationToNotificationDto(Notification notification);

	/**
	 * Transforms a list of notification objects into a list of DTOs
	 * 
	 * @param prices the domain objects
	 * @return the dtos
	 */
	List<NotificationDto> notificationsToNotificationDtos(List<Notification> notifications);
}