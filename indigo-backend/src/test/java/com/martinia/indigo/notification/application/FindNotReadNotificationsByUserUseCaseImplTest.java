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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


public class FindNotReadNotificationsByUserUseCaseImplTest extends BaseIndigoTest {

	@InjectMocks
	private FindNotReadNotificationsByUserUseCaseImpl findNotReadNotificationsByUserUseCase;

	@MockBean
	private NotificationRepository notificationRepository;

	@Test
	public void findNotReadUser_NotificationsExist_ReturnsListOfNotifications() {
		// Given
		String user = "john";

		Notification notification1 = new Notification();
		notification1.setId("1");
		notification1.setUser(user);
		notification1.setReadUser(false);

		Notification notification2 = new Notification();
		notification2.setId("2");
		notification2.setUser(user);
		notification2.setReadUser(false);

		List<Notification> expectedNotifications = Arrays.asList(notification1, notification2);

		when(notificationRepository.findByUserAndReadUserFalse(user)).thenReturn(expectedNotifications);

		// When
		List<Notification> result = findNotReadNotificationsByUserUseCase.findNotReadUser(user);

		// Then
		assertEquals(expectedNotifications, result);
	}
}
