package com.martinia.indigo.notification.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.notification.domain.ports.usecases.MarkAsReadNotificationUseCase;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;

import javax.annotation.Resource;

public class MarkAsReadNotificationUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private MarkAsReadNotificationUseCase markAsReadNotificationUseCase;

	@MockBean
	private NotificationRepository notificationRepository;

	@MockBean
	private UserRepository userRepository;

	@Test
	public void testMarkAsRead_AdminUser_MarksNotificationAsReadAdmin() {
		// Given
		String id = "notification_id";
		String user = "admin_user";

		NotificationMongoEntity notificationEntity = new NotificationMongoEntity();
		notificationEntity.setId(id);

		UserMongoEntity adminUser = new UserMongoEntity();
		adminUser.setRole("ADMIN");

		when(notificationRepository.findById(id)).thenReturn(Optional.of(notificationEntity));
		when(userRepository.findById(user)).thenReturn(Optional.of(adminUser));

		// When
		markAsReadNotificationUseCase.markAsRead(id, user);

		// Then
		verify(notificationRepository).save(notificationEntity);
		assertTrue(notificationEntity.isReadAdmin());
		assertFalse(notificationEntity.isReadUser());
	}

	@Test
	public void testMarkAsRead_NonAdminUser_MarksNotificationAsReadUser() {
		// Given
		String id = "notification_id";
		String user = "regular_user";

		NotificationMongoEntity notificationEntity = new NotificationMongoEntity();
		notificationEntity.setId(id);

		UserMongoEntity regularUser = new UserMongoEntity();
		regularUser.setRole("USER");

		when(notificationRepository.findById(id)).thenReturn(Optional.of(notificationEntity));
		when(userRepository.findById(user)).thenReturn(Optional.of(regularUser));

		// When
		markAsReadNotificationUseCase.markAsRead(id, user);

		// Then
		verify(notificationRepository).save(notificationEntity);
		assertFalse(notificationEntity.isReadAdmin());
		assertTrue(notificationEntity.isReadUser());
	}

	@Test
	public void testMarkAsRead_NotificationNotFound_DoesNotSave() {
		// Given
		String id = "non_existing_id";
		String user = "regular_user";

		when(notificationRepository.findById(id)).thenReturn(Optional.empty());
		when(userRepository.findById(user)).thenReturn(Optional.of(new UserMongoEntity()));

		// When
		markAsReadNotificationUseCase.markAsRead(id, user);

		// Then
		verify(notificationRepository).findById(id);
		verify(notificationRepository, never()).save(Mockito.any(NotificationMongoEntity.class));
	}

}
