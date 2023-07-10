package com.martinia.indigo.notification.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


public class FindNotReadNotificationsUseCaseImplTest  extends BaseIndigoTest {

	@InjectMocks
	private FindNotReadNotificationsUseCaseImpl findNotReadNotificationsUseCase;

	@MockBean
	private NotificationRepository notificationRepository;

	@Test
	public void findNotReadAdmin_NotificationsExist_ReturnsListOfNotifications() {
		// Given
		Notification notification1 = new Notification();
		notification1.setId("1");
		notification1.setReadAdmin(false);

		Notification notification2 = new Notification();
		notification2.setId("2");
		notification2.setReadAdmin(false);

		List<Notification> expectedNotifications = Arrays.asList(notification1, notification2);

		when(notificationRepository.findByReadAdminFalse()).thenReturn(expectedNotifications);

		// When
		List<Notification> result = findNotReadNotificationsUseCase.findNotReadAdmin();

		// Then
		assertEquals(expectedNotifications, result);
	}
}
