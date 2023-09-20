package com.martinia.indigo.notification.infrastructure.api;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.notification.domain.model.NotificationEnum;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.annotation.Resource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MarkAsReadNotificationControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Resource
	private MockMvc mockMvc;

	private NotificationMongoEntity notificationEntity;

	private NotificationMongoEntity notificationEntity2;

	private UserMongoEntity userMongoEntity;

	private UserMongoEntity adminMongoEntity;

	@BeforeEach
	public void init() {
		notificationEntity = NotificationMongoEntity.builder()
				.id("1")
				.book("book")
				.user("user")
				.type(NotificationEnum.KINDLE.name())
				.build();
		notificationRepository.save(notificationEntity);

		notificationEntity2 = NotificationMongoEntity.builder()
				.id("2")
				.book("book2")
				.user("user")
				.type(NotificationEnum.KINDLE.name())
				.build();
		notificationRepository.save(notificationEntity2);

		userMongoEntity = UserMongoEntity.builder().id("1").username("user").role("USER").build();
		userRepository.save(userMongoEntity);

		adminMongoEntity = UserMongoEntity.builder().id("1").username("admin").role("ADMIN").build();
		userRepository.save(adminMongoEntity);
	}

	@Test
	public void markAsReadByAdmin() throws Exception {
		// Given
		String id = notificationEntity.getId();
		String user = adminMongoEntity.getId();

		// When
		final ResultActions result = mockMvc.perform(
				get("/api/notification/read").param("id", id).param("user", user).contentType(MediaType.APPLICATION_JSON_VALUE));

		// Then
		result.andExpect(status().isOk());

	}

	@Test
	public void markAsReadByUser() throws Exception {
		// Given
		String id = notificationEntity.getId();
		String user = userMongoEntity.getId();

		// When
		final ResultActions result = mockMvc.perform(
				get("/api/notification/read").param("id", id).param("user", user).contentType(MediaType.APPLICATION_JSON_VALUE));

		// Then
		result.andExpect(status().isOk());

	}

	@Test
	public void markAsReadIdNotFound() throws Exception {
		// Given
		String id = "unknown";
		String user = notificationEntity.getUser();

		// When
		final ResultActions result = mockMvc.perform(
				get("/api/notification/read").param("id", id).param("user", user).contentType(MediaType.APPLICATION_JSON_VALUE));

		// Then
		result.andExpect(status().isOk());

	}

	@Test
	public void markAsReadUserNotFound() throws Exception {
		// Given
		String id = notificationEntity.getId();
		String user = "unknown";

		// When
		final ResultActions result = mockMvc.perform(
				get("/api/notification/read").param("id", id).param("user", user).contentType(MediaType.APPLICATION_JSON_VALUE));

		// Then
		result.andExpect(status().isOk());

	}
}
