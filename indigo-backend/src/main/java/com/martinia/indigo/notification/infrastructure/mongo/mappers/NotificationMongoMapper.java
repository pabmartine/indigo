package com.martinia.indigo.notification.infrastructure.mongo.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import com.martinia.indigo.notification.domain.model.Notification;

@Mapper(componentModel = "spring")
public interface NotificationMongoMapper {

	NotificationMongoEntity domain2Entity(Notification domain);

	Notification entity2Domain(NotificationMongoEntity entity);

	List<NotificationMongoEntity> domains2Entities(List<Notification> domains);

	List<Notification> entities2Domains(List<NotificationMongoEntity> entities);

}