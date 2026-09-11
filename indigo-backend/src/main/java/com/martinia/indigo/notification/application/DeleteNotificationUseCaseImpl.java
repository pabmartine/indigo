package com.martinia.indigo.notification.application;

import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import com.martinia.indigo.notification.domain.ports.usecases.DeleteNotificationUseCase;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteNotificationUseCaseImpl implements DeleteNotificationUseCase {

	@Resource
	private NotificationRepository notificationRepository;

	@Resource
	private UserRepository userRepository;

	@Override
	public void delete(final String id, final String user) {
		userRepository.findByUsername(user).ifPresent(usr -> notificationRepository.findById(id).ifPresent(notification -> {
			if ("ADMIN".equalsIgnoreCase(usr.getRole()) || java.util.Objects.equals(usr.getUsername(), notification.getUser())) {
				notificationRepository.delete(notification);
			}
		}));
	}

}
