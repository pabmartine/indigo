package com.martinia.indigo.notification.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.verify;


public class MarkAsReadNotificationUseCaseImplTest extends BaseIndigoTest {

	@InjectMocks
	private MarkAsReadNotificationUseCaseImpl markAsReadNotificationUseCase;

	@MockBean
	private NotificationRepository notificationRepository;

	@Test
	public void markAsRead_NotificationExists_MarksNotificationAsRead() {
		// Given
		String id = "notificationId";
		String user = "john";

		// When
		markAsReadNotificationUseCase.markAsRead(id, user);

		// Then
		verify(notificationRepository).markAsRead(id, user);
	}
}
