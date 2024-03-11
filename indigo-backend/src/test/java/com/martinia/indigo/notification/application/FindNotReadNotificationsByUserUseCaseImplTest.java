package com.martinia.indigo.notification.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import com.martinia.indigo.notification.domain.ports.usecases.FindNotReadNotificationsByUserUseCase;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import com.martinia.indigo.notification.infrastructure.mongo.mappers.NotificationMongoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class FindNotReadNotificationsByUserUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindNotReadNotificationsByUserUseCase findNotReadNotificationsByUserUseCase;

	@MockBean
	private NotificationRepository notificationRepository;

	@MockBean
	private NotificationMongoMapper notificationMongoMapper;

	@Test
	public void testFindNotReadUser_ReturnsNotReadNotificationsByUser() {
		// Given
		String user = "john_doe";

		List<NotificationMongoEntity> notificationEntities = new ArrayList<>();
		notificationEntities.add(NotificationMongoEntity.builder().id("1").build());
		notificationEntities.add(NotificationMongoEntity.builder().id("2").build());
		notificationEntities.add(NotificationMongoEntity.builder().id("3").build());

		List<Notification> expectedNotifications = new ArrayList<>();
		expectedNotifications.add(Notification.builder().id("1").build());
		expectedNotifications.add(Notification.builder().id("2").build());
		expectedNotifications.add(Notification.builder().id("3").build());

		when(notificationRepository.findByUserAndReadUserIsFalse(user)).thenReturn(notificationEntities);
		when(notificationMongoMapper.entities2Domains(notificationEntities)).thenReturn(expectedNotifications);

		// When
		List<Notification> result = findNotReadNotificationsByUserUseCase.findNotReadUser(user);

		// Then
		assertEquals(expectedNotifications, result);
	}
}
