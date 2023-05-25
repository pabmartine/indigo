package com.martinia.indigo.notification.application;

import com.martinia.indigo.notification.domain.repository.NotificationRepository;
import com.martinia.indigo.notification.domain.service.MarkAsReadNotificationUseCase;
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
