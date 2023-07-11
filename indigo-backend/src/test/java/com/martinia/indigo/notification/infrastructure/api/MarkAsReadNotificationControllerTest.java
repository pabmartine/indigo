package com.martinia.indigo.notification.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.notification.domain.ports.usecases.MarkAsReadNotificationUseCase;
import com.martinia.indigo.notification.infrastructure.api.controllers.MarkAsReadNotificationController;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.Resource;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class MarkAsReadNotificationControllerTest extends BaseIndigoTest {

	@InjectMocks
	private MarkAsReadNotificationController markAsReadNotificationController;

	@MockBean
	private MarkAsReadNotificationUseCase markAsReadNotificationUseCase;

	@Resource
	private MockMvc mockMvc;

	@Test
	public void markAsRead_ValidIdAndUser_ReturnsHttpStatusOK() throws Exception {
		// Given
		String id = "1";
		String user = "john";

		// When
		mockMvc.perform(get("/api/notification/read")
						.param("id", id)
						.param("user", user)
						.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andReturn();

		// Then
		verify(markAsReadNotificationUseCase).markAsRead(id, user);
	}
}
