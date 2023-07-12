package com.martinia.indigo.notification.application;

import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import com.martinia.indigo.notification.domain.ports.usecases.SaveNotificationUseCase;
import com.martinia.indigo.notification.infrastructure.mongo.mappers.NotificationMongoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SaveNotificationUseCaseImpl implements SaveNotificationUseCase {

	@Resource
	private NotificationRepository notificationRepository;

	@Resource
	private NotificationMongoMapper notificationMongoMapper;

	@Override
	public void save(final Notification notification) {
		notificationRepository.save(notificationMongoMapper.domain2Entity(notification));
	}
}
