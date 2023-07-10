package com.martinia.indigo.notification.application;

import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import com.martinia.indigo.notification.domain.ports.usecases.SaveNotificationUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SaveNotificationUseCaseImpl implements SaveNotificationUseCase {

	@Resource
	private NotificationRepository notificationRepository;

	@Override
	public void save(final Notification notification) {
		notificationRepository.save(notification);
	}
}
