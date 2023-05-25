package com.martinia.indigo.notification.application;

import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.repository.NotificationRepository;
import com.martinia.indigo.notification.domain.service.FindAllNotificationsUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class FindAllNotificationsUseCaseImpl implements FindAllNotificationsUseCase {

	@Resource
	private NotificationRepository notificationRepository;

	@Override
	public List<Notification> findAllByOrderBySendDateDesc() {
		return notificationRepository.findAllByOrderBySendDateDesc();
	}
}
