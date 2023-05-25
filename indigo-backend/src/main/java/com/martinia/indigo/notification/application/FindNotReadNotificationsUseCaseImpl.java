package com.martinia.indigo.notification.application;

import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.repository.NotificationRepository;
import com.martinia.indigo.notification.domain.service.FindNotReadNotificationsUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class FindNotReadNotificationsUseCaseImpl implements FindNotReadNotificationsUseCase {

	@Resource
	private NotificationRepository notificationRepository;

	@Override
	public List<Notification> findNotReadAdmin() {
		return notificationRepository.findByReadAdminFalse();
	}
}
