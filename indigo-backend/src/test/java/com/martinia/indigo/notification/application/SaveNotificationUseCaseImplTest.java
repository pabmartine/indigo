package com.martinia.indigo.notification.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import com.martinia.indigo.notification.domain.ports.usecases.SaveNotificationUseCase;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import com.martinia.indigo.notification.infrastructure.mongo.mappers.NotificationMongoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SaveNotificationUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private SaveNotificationUseCase saveNotificationUseCase;

	@MockBean
	private NotificationRepository notificationRepository;

	@MockBean
	private NotificationMongoMapper notificationMongoMapper;

	@Test
	public void testSave_SavesNotification() {
		// Given
		Notification notification = Notification.builder().id("1").build();
		NotificationMongoEntity notificationEntity = NotificationMongoEntity.builder().id("1").build();

		when(notificationMongoMapper.domain2Entity(notification)).thenReturn(notificationEntity);

		// When
		saveNotificationUseCase.save(notification);

		// Then
		verify(notificationRepository).save(notificationEntity);
	}
}
