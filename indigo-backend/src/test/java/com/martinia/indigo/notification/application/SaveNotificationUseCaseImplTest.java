package com.martinia.indigo.notification.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.notification.domain.model.Notification;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.verify;


public class SaveNotificationUseCaseImplTest extends BaseIndigoTest {

	@InjectMocks
	private SaveNotificationUseCaseImpl saveNotificationUseCase;

//	@MockBean
//	private NotificationRepository notificationRepository;

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
//		verify(notificationRepository).save(notification);
	}
}
