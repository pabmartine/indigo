package com.martinia.indigo.notification.application;

import com.martinia.indigo.notification.domain.repository.NotificationRepository;
import com.martinia.indigo.notification.domain.service.DeleteNotificationUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DeleteNotificationUseCaseImpl implements DeleteNotificationUseCase {

	@Resource
	private NotificationRepository notificationRepository;

	@Override
	public void delete(final String id) {
		notificationRepository.delete(id);
	}
}
