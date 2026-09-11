package com.martinia.indigo.notification.application;

import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import com.martinia.indigo.notification.domain.ports.usecases.MarkAsReadNotificationUseCase;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class MarkAsReadNotificationUseCaseImpl implements MarkAsReadNotificationUseCase {

	@Resource
	private NotificationRepository notificationRepository;

	@Resource
	private UserRepository userRepository;

	@Override
	public void markAsRead(final String id, final String user) {
		java.util.Optional<com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity> userOpt = userRepository.findById(user);
		if (userOpt.isEmpty()) {
			userOpt = userRepository.findByUsername(user);
		}
		userOpt.ifPresent(usr -> {
			notificationRepository.findById(id).ifPresent(notification -> {
				if ("ADMIN".equalsIgnoreCase(usr.getRole())) {
					notification.setReadAdmin(true);
					notificationRepository.save(notification);
				}
				else if (Objects.equals(usr.getUsername(), notification.getUser())) {
					notification.setReadUser(true);
					notificationRepository.save(notification);
				}
			});
		});
	}
}
