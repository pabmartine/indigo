package com.martinia.indigo.notification.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.verify;


public class SaveNotificationUseCaseImplTest extends BaseIndigoTest {

	@InjectMocks
	private SaveNotificationUseCaseImpl saveNotificationUseCase;

	@MockBean
	private NotificationRepository notificationRepository;

	@Test
	public void save_Notification_SavesNotification() {
		// Given
		Notification notification = new Notification();
		notification.setId("1");
		notification.setType("type");
		// Set other properties of the notification

		// When
		saveNotificationUseCase.save(notification);

		// Then
		verify(notificationRepository).save(notification);
	}
}
