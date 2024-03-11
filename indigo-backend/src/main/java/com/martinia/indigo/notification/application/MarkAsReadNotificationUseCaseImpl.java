package com.martinia.indigo.notification.application;

import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import com.martinia.indigo.notification.domain.ports.usecases.MarkAsReadNotificationUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Service
@Transactional
public class MarkAsReadNotificationUseCaseImpl implements MarkAsReadNotificationUseCase {

	@Resource
	private NotificationRepository notificationRepository;

	@Resource
	private UserRepository userRepository;

	@Override
	public void markAsRead(final String id, final String user) {
		userRepository.findById(user).ifPresent(usr -> {
			notificationRepository.findById(id).ifPresent(notification -> {
				if (usr.getRole().equals("ADMIN")) {
					notification.setReadAdmin(true);
				}
				else {
					notification.setReadUser(true);
				}
				notificationRepository.save(notification);
			});
		});
	}
}
