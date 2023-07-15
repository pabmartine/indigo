package com.martinia.indigo.notification.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import com.martinia.indigo.notification.domain.ports.usecases.DeleteNotificationUseCase;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeleteNotificationUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private DeleteNotificationUseCase deleteNotificationUseCase;

	@MockBean
	private NotificationRepository notificationRepository;

	@Test
	public void testDelete_DeletesNotificationById() {
		// Given
		String id = "notification_id";

		NotificationMongoEntity notificationEntity = new NotificationMongoEntity();
		notificationEntity.setId(id);

		when(notificationRepository.findById(id)).thenReturn(Optional.of(notificationEntity));

		// When
		deleteNotificationUseCase.delete(id);

		// Then
		verify(notificationRepository).delete(notificationEntity);
	}

	@Test
	public void testDelete_DoesNotDeleteWhenNotificationNotFound() {
		// Given
		String id = "non_existing_id";

		when(notificationRepository.findById(id)).thenReturn(Optional.empty());

		// When
		deleteNotificationUseCase.delete(id);

		// Then
		verify(notificationRepository).findById(id);
		verify(notificationRepository).delete(any());
	}
}
