package com.martinia.indigo.notification.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.notification.infrastructure.api.controllers.FindNotReadNotificationsController;
import com.martinia.indigo.notification.infrastructure.api.model.NotificationDto;
import com.martinia.indigo.notification.infrastructure.api.mappers.NotificationDtoMapper;
import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.ports.usecases.FindNotReadNotificationsUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class FindNotReadNotificationsControllerTest extends BaseIndigoTest {

	@InjectMocks
	private FindNotReadNotificationsController findNotReadNotificationsController;

	@MockBean
	private FindNotReadNotificationsUseCase findNotReadNotificationsUseCase;

	@MockBean
	private NotificationDtoMapper notificationDtoMapper;

	@Resource
	private MockMvc mockMvc;

	@Test
	public void findAllNotRead_ReturnsListOfNotificationDtos() throws Exception {
		// Given
		Notification notification1 = new Notification();
		notification1.setId("1");
		// Set other properties of the notification1

		Notification notification2 = new Notification();
		notification2.setId("2");
		// Set other properties of the notification2

		List<Notification> notifications = Arrays.asList(notification1, notification2);

		NotificationDto notificationDto1 = new NotificationDto();
		notificationDto1.setId("1");
		// Set other properties of the notificationDto1

		NotificationDto notificationDto2 = new NotificationDto();
		notificationDto2.setId("2");
		// Set other properties of the notificationDto2

		List<NotificationDto> expectedNotificationDtos = Arrays.asList(notificationDto1, notificationDto2);

		when(findNotReadNotificationsUseCase.findNotReadAdmin())
				.thenReturn(notifications);

		when(notificationDtoMapper.domains2Dtos(notifications))
				.thenReturn(expectedNotificationDtos);

		// When
		mockMvc.perform(get("/api/notification/not_read")
						.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].id", is("1")))
				.andExpect(jsonPath("$[1].id", is("2")))
				// Add assertions for other properties of NotificationDto
				.andReturn();
	}
}
