package com.martinia.indigo.notification.infrastructure.api;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.notification.domain.model.NotificationEnum;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FindNotReadNotificationsControllerIntegrationTest extends BaseIndigoIntegrationTest {

	private NotificationMongoEntity notificationEntity;

	private NotificationMongoEntity notificationEntity2;

	@BeforeEach
	public void init() {
		notificationEntity = NotificationMongoEntity.builder()
				.id("1")
				.book("book")
				.user("user")
				.readAdmin(true)
				.type(NotificationEnum.KINDLE.name())
				.build();
		notificationRepository.save(notificationEntity);

		notificationEntity2 = NotificationMongoEntity.builder()
				.id("2")
				.book("book2")
				.user("user")
				.readAdmin(false)
				.type(NotificationEnum.KINDLE.name())
				.build();
		notificationRepository.save(notificationEntity2);
	}

	@Test
	public void findAll() throws Exception {
		// Given

		// When
		final ResultActions result = mockMvc.perform(get("/api/notification/not_read").contentType(MediaType.APPLICATION_JSON_VALUE));

		//Then
		result.andExpect(status().isOk());

		result.andExpect(jsonPath("$", hasSize(1)));
		result.andExpect(jsonPath("$[0].id", is("2")));
	}
}
