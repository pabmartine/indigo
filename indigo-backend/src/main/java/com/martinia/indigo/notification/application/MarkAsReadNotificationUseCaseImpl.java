package com.martinia.indigo.notification.application;

import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import com.martinia.indigo.notification.domain.ports.usecases.MarkAsReadNotificationUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MarkAsReadNotificationUseCaseImpl implements MarkAsReadNotificationUseCase {

	@Resource
	private NotificationRepository notificationRepository;

	@Override
	public void markAsRead(final String id, final String user) {
		notificationRepository.markAsRead(id, user);
	}
}
