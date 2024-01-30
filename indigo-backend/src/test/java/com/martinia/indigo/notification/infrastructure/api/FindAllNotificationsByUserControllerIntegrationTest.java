package com.martinia.indigo.notification.infrastructure.api;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.notification.domain.model.NotificationEnum;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FindAllNotificationsByUserControllerIntegrationTest extends BaseIndigoIntegrationTest {

	private NotificationMongoEntity notificationEntity;

	private NotificationMongoEntity notificationEntity2;

	@BeforeEach
	public void init() {
		notificationEntity = NotificationMongoEntity.builder()
				.id("1")
				.book("book")
				.user("user")
				.type(NotificationEnum.KINDLE.name())
				.build();
		notificationRepository.save(notificationEntity);

		notificationEntity2 = NotificationMongoEntity.builder().id("2").book("book2").user("user").type("type").build();
		notificationRepository.save(notificationEntity2);
	}

	@Test
	public void findAllNotifications() throws Exception {
		// Given

		final String user = "user";

		// When
		final ResultActions result = mockMvc.perform(
				get("/api/notification/user").param("user", user).contentType(MediaType.APPLICATION_JSON_VALUE));

		//Then
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$[0].id", is("1")));
		result.andExpect(jsonPath("$[1].id").doesNotExist());
	}

	@Test
	public void findAllNotificationsUnknownUser() throws Exception {
		// Given

		final String user = "unknown";

		// When
		final ResultActions result = mockMvc.perform(
				get("/api/notification/user").param("user", user).contentType(MediaType.APPLICATION_JSON_VALUE));

		//Then
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$[0].id").doesNotExist());
	}
}
