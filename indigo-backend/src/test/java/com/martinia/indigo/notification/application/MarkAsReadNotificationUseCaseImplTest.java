package com.martinia.indigo.notification.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.notification.domain.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
