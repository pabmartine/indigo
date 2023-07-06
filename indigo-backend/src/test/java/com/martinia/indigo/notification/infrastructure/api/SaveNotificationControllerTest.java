package com.martinia.indigo.notification.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.adapters.in.rest.dtos.NotificationDto;
import com.martinia.indigo.adapters.in.rest.mappers.NotificationDtoMapper;
import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.domain.service.SaveNotificationUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.Resource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class SaveNotificationControllerTest extends BaseIndigoTest {


	@MockBean
	private SaveNotificationUseCase saveNotificationUseCase;

	@MockBean
	private NotificationDtoMapper notificationDtoMapper;

	@Resource
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	public void save_ValidNotificationDto_ReturnsHttpStatusOK() throws Exception {
		// Given
		NotificationDto notificationDto = new NotificationDto();
		// Set properties of the notificationDto

		Notification notification = new Notification();
		// Set properties of the notification

		when(notificationDtoMapper.dto2Domain(notificationDto)).thenReturn(notification);

		// When
		mockMvc.perform(put("/rest/notification/save").content(asJsonString(notificationDto)).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk()).andReturn();

		// Then
		verify(saveNotificationUseCase).save(any());
	}

	// Utility method to convert object to JSON string
	private static String asJsonString(Object obj) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(obj);
	}
}
