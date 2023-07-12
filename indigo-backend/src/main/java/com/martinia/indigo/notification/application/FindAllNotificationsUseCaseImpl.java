package com.martinia.indigo.notification.application;

import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import com.martinia.indigo.notification.domain.ports.usecases.FindAllNotificationsUseCase;
import com.martinia.indigo.notification.infrastructure.mongo.mappers.NotificationMongoMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class FindAllNotificationsUseCaseImpl implements FindAllNotificationsUseCase {

	@Resource
	private NotificationRepository notificationRepository;

	@Resource
	private NotificationMongoMapper notificationMongoMapper;

	@Override
	public List<Notification> findAllByOrderBySendDateDesc() {
		return notificationMongoMapper.entities2Domains(notificationRepository.findAll(Sort.by(Sort.Direction.DESC, "sendDate")));
	}
}
