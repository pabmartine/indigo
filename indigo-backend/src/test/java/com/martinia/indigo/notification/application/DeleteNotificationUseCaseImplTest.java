package com.martinia.indigo.notification.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.notification.domain.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
