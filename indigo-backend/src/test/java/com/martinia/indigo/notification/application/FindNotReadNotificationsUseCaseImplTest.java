package com.martinia.indigo.notification.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import com.martinia.indigo.notification.domain.ports.usecases.FindNotReadNotificationsUseCase;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import com.martinia.indigo.notification.infrastructure.mongo.mappers.NotificationMongoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class FindNotReadNotificationsUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindNotReadNotificationsUseCase findNotReadNotificationsUseCase;

	@MockBean
	private NotificationRepository notificationRepository;

	@MockBean
	private NotificationMongoMapper notificationMongoMapper;

	@Test
	public void testFindNotReadAdmin_ReturnsNotReadNotifications() {
		// Given
		List<NotificationMongoEntity> notificationEntities = new ArrayList<>();
		notificationEntities.add(NotificationMongoEntity.builder().id("1").build());
		notificationEntities.add(NotificationMongoEntity.builder().id("2").build());
		notificationEntities.add(NotificationMongoEntity.builder().id("3").build());

		List<Notification> expectedNotifications = new ArrayList<>();
		expectedNotifications.add(Notification.builder().id("1").build());
		expectedNotifications.add(Notification.builder().id("2").build());
		expectedNotifications.add(Notification.builder().id("3").build());

		when(notificationRepository.findByReadAdminIsFalse()).thenReturn(notificationEntities);
		when(notificationMongoMapper.entities2Domains(notificationEntities)).thenReturn(expectedNotifications);

		// When
		List<Notification> result = findNotReadNotificationsUseCase.findNotReadAdmin();

		// Then
		assertEquals(expectedNotifications, result);
	}
}
