package com.martinia.indigo.notification.application;

import com.martinia.indigo.notification.domain.model.NotificationEnum;
import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.repository.NotificationRepository;
import com.martinia.indigo.notification.domain.service.FindAllNotificationsByUserUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class FindAllNotificationsByUserUseCaseImpl implements FindAllNotificationsByUserUseCase {

	@Resource
	private NotificationRepository notificationRepository;

	@Override
	public List<Notification> findByUser(final String user) {
		return notificationRepository.findByUserAndType(user, NotificationEnum.KINDLE);
	}
}
