package com.martinia.indigo.notification.infrastructure.api;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DeleteNotificationControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Resource
	private MockMvc mockMvc;

	private NotificationMongoEntity notificationEntity;

	@BeforeEach
	public void init() {
		notificationEntity = NotificationMongoEntity.builder().id("1").book("book").user("user").type("type").build();
		notificationRepository.save(notificationEntity);
	}

	@Test
	@WithMockUser
	public void deleteNotificationOK() throws Exception {
		// Given

		// When
		final ResultActions result = mockMvc.perform(
				delete("/api/notification/delete").param("id", notificationEntity.getId()).contentType(MediaType.APPLICATION_JSON_VALUE));

		// Then
		result.andExpect(status().isOk());
		assertTrue(notificationRepository.findById(notificationEntity.getId()).isEmpty());
	}

	@Test
	@WithMockUser
	public void deleteNotificationNotFound() throws Exception {
		// Given
		String id = "unknown";

		// When
		final ResultActions result = mockMvc.perform(
				delete("/api/notification/delete").param("id", id).contentType(MediaType.APPLICATION_JSON_VALUE));

		// Then
		result.andExpect(status().isOk());
		assertTrue(notificationRepository.findById(notificationEntity.getId()).isPresent());
	}
}
