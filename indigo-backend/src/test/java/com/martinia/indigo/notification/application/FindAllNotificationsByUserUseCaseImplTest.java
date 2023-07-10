package com.martinia.indigo.notification.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.notification.domain.model.NotificationEnum;
import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


public class FindAllNotificationsByUserUseCaseImplTest extends BaseIndigoTest {

	@InjectMocks
	private FindAllNotificationsByUserUseCaseImpl findAllNotificationsByUserUseCase;

	@MockBean
	private NotificationRepository notificationRepository;

	@Test
	public void findByUser_NotificationsExist_ReturnsListOfNotifications() {
		// Given
		String user = "john";

		Notification notification1 = new Notification();
		notification1.setId("1");
		notification1.setUser(user);
		notification1.setType(NotificationEnum.KINDLE.name());

		Notification notification2 = new Notification();
		notification2.setId("2");
		notification2.setUser(user);
		notification2.setType(NotificationEnum.KINDLE.name());

		List<Notification> expectedNotifications = Arrays.asList(notification1, notification2);

		when(notificationRepository.findByUserAndType(user, NotificationEnum.KINDLE)).thenReturn(expectedNotifications);

		// When
		List<Notification> result = findAllNotificationsByUserUseCase.findByUser(user);

		// Then
		assertEquals(expectedNotifications, result);
	}
}