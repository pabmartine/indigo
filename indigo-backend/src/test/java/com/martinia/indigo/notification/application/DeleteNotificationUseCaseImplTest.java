package com.martinia.indigo.notification.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.verify;


public class DeleteNotificationUseCaseImplTest  extends BaseIndigoTest {

	@InjectMocks
	private DeleteNotificationUseCaseImpl deleteNotificationUseCase;

	@MockBean
	private NotificationRepository notificationRepository;

	@Test
	public void delete_NotificationExists_DeletesNotification() {
		// Given
		String notificationId = "notificationId";

		// When
		deleteNotificationUseCase.delete(notificationId);

		// Then
		verify(notificationRepository).delete(notificationId);
	}
}
