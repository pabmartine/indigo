package com.martinia.indigo.notification.infrastructure.api;

import com.martinia.indigo.notification.domain.service.MarkAsReadNotificationUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.Resource;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@AutoConfigureMockMvc
public class MarkAsReadNotificationControllerTest {

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
		mockMvc.perform(get("/rest/notification/read")
						.param("id", id)
						.param("user", user)
						.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andReturn();

		// Then
		verify(markAsReadNotificationUseCase).markAsRead(id, user);
	}
}
