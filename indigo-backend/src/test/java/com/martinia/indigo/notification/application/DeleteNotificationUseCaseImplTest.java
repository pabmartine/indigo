package com.martinia.indigo.notification.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import com.martinia.indigo.notification.domain.ports.usecases.DeleteNotificationUseCase;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import jakarta.annotation.Resource;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;

public class DeleteNotificationUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private DeleteNotificationUseCase deleteNotificationUseCase;

	@MockBean
	private NotificationRepository notificationRepository;

	@MockBean
	private UserRepository userRepository;

	@Test
	public void testDelete_DeletesNotificationById() {
		// Given
		String id = "notification_id";

		NotificationMongoEntity notificationEntity = new NotificationMongoEntity();
		notificationEntity.setId(id);
		notificationEntity.setUser("user");
		UserMongoEntity user = new UserMongoEntity();
		user.setUsername("user");
		user.setRole("USER");

		when(notificationRepository.findById(id)).thenReturn(Optional.of(notificationEntity));
		when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

		// When
		deleteNotificationUseCase.delete(id, "user");

		// Then
		verify(notificationRepository).delete(notificationEntity);
	}

	@Test
	public void testDelete_DoesNotDeleteWhenNotificationNotFound() {
		// Given
		String id = "non_existing_id";

		when(notificationRepository.findById(id)).thenReturn(Optional.empty());

		// When
		deleteNotificationUseCase.delete(id, "user");

		// Then
		verify(notificationRepository).findById(id);
		verify(notificationRepository, times(0)).deleteById(id);
	}

	@Test
	public void testDelete_DoesNotDeleteAnotherUsersNotification() {
		String id = "notification_id";
		NotificationMongoEntity notificationEntity = new NotificationMongoEntity();
		notificationEntity.setId(id);
		notificationEntity.setUser("owner");
		UserMongoEntity user = new UserMongoEntity();
		user.setUsername("other-user");
		user.setRole("USER");
		when(userRepository.findByUsername("other-user")).thenReturn(Optional.of(user));
		when(notificationRepository.findById(id)).thenReturn(Optional.of(notificationEntity));

		deleteNotificationUseCase.delete(id, "other-user");

		verify(notificationRepository, never()).delete(notificationEntity);
	}
}
