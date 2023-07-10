package com.martinia.indigo.notification.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


public class FindAllNotificationsUseCaseImplTest extends BaseIndigoTest {

	@InjectMocks
	private FindAllNotificationsUseCaseImpl findAllNotificationsUseCase;

	@MockBean
	private NotificationRepository notificationRepository;

	@Test
	public void findAllByOrderBySendDateDesc_NotificationsExist_ReturnsListOfNotifications() {
		// Given
		Notification notification1 = new Notification();
		notification1.setId("1");
		notification1.setSendDate(new Date(2023, 4, 10));

		Notification notification2 = new Notification();
		notification2.setId("2");
		notification2.setSendDate(new Date(2023, 4, 11));

		List<Notification> expectedNotifications = Arrays.asList(notification2, notification1);

		when(notificationRepository.findAllByOrderBySendDateDesc()).thenReturn(expectedNotifications);

		// When
		List<Notification> result = findAllNotificationsUseCase.findAllByOrderBySendDateDesc();

		// Then
		assertEquals(expectedNotifications, result);
	}
}
