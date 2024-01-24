package com.martinia.indigo.notification.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.notification.domain.model.NotificationEnum;
import com.martinia.indigo.notification.infrastructure.api.model.NotificationDto;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SaveNotificationControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@Test
	@WithMockUser
	public void saveNotificationOk() throws Exception {
		// Given

		NotificationDto notificationDto = NotificationDto.builder().id("1").book("book").user("user").type(NotificationEnum.KINDLE).build();

		// When
		final ResultActions result = mockMvc.perform(
				put("/api/notification/save").content(mapAsJsonString(notificationDto)).contentType(MediaType.APPLICATION_JSON_VALUE));

		// Then
		result.andExpect(status().isOk());
		assertEquals(1, notificationRepository.findAll().size());
		final NotificationMongoEntity notificationMongoEntity = notificationRepository.findAll().stream().findFirst().get();
		assertEquals(notificationDto.getId(), notificationMongoEntity.getId());
		assertEquals(notificationDto.getBook(), notificationMongoEntity.getBook());
		assertEquals(notificationDto.getUser(), notificationMongoEntity.getUser());
		assertEquals(notificationDto.getType().name(), notificationMongoEntity.getType());
	}

	private static String mapAsJsonString(Object obj) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(obj);
	}
}
