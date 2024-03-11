package com.martinia.indigo.notification.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.model.NotificationEnum;
import com.martinia.indigo.notification.domain.ports.usecases.FindNotReadNotificationsByUserUseCase;
import com.martinia.indigo.notification.infrastructure.api.controllers.FindNotReadNotificationsByUserController;
import com.martinia.indigo.notification.infrastructure.api.mappers.NotificationDtoMapper;
import com.martinia.indigo.notification.infrastructure.api.model.NotificationDto;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FindNotReadNotificationsByUserControllerIntegrationTest extends BaseIndigoTest {

	@Resource
	private MockMvc mockMvc;

	private NotificationMongoEntity notificationEntity;

	private NotificationMongoEntity notificationEntity2;

	@BeforeEach
	public void init() {
		notificationEntity = NotificationMongoEntity.builder()
				.id("1")
				.book("book")
				.user("user")
				.readUser(true)
				.type(NotificationEnum.KINDLE.name())
				.build();
		notificationRepository.save(notificationEntity);

		notificationEntity2 = NotificationMongoEntity.builder()
				.id("2")
				.book("book2")
				.user("user")
				.readUser(false)
				.type(NotificationEnum.KINDLE.name())
				.build();
		notificationRepository.save(notificationEntity2);
	}

	@Test
	public void findAll() throws Exception {
		// Given
		final String user = "user";

		// When
		final ResultActions result = mockMvc.perform(
				get("/api/notification/not_read_user").param("user", user).contentType(MediaType.APPLICATION_JSON_VALUE));

		//Then
		result.andExpect(status().isOk());

		result.andExpect(jsonPath("$", hasSize(1)));
		result.andExpect(jsonPath("$[0].id", is("2")));
	}

	@Test
	public void findAllUserNotFound() throws Exception {
		// Given
		final String user = "unknown";

		// When
		final ResultActions result = mockMvc.perform(
				get("/api/notification/not_read_user").param("user", user).contentType(MediaType.APPLICATION_JSON_VALUE));

		//Then
		result.andExpect(status().isOk());

		result.andExpect(jsonPath("$", hasSize(0)));
	}
}
