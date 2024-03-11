package com.martinia.indigo.notification.application;

import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.model.NotificationEnum;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import com.martinia.indigo.notification.domain.ports.usecases.FindAllNotificationsByUserUseCase;
import com.martinia.indigo.notification.infrastructure.mongo.mappers.NotificationMongoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class FindAllNotificationsByUserUseCaseImpl implements FindAllNotificationsByUserUseCase {

	@Resource
	private NotificationRepository notificationRepository;

	@Resource
	private NotificationMongoMapper notificationMongoMapper;

	@Override
	public List<Notification> findByUser(final String user) {
		return notificationMongoMapper.entities2Domains(notificationRepository.findByUserAndType(user, NotificationEnum.KINDLE.name()));
	}
}
