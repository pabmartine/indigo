package com.martinia.indigo.notification.infrastructure.api;

import com.martinia.indigo.notification.domain.service.DeleteNotificationUseCase;
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

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@AutoConfigureMockMvc
public class DeleteNotificationControllerTest {

	@InjectMocks
	private DeleteNotificationController deleteNotificationController;

	@MockBean
	private DeleteNotificationUseCase deleteNotificationUseCase;

	@Resource
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	public void delete_ValidId_DeletesNotificationAndReturnsOkResponse() throws Exception {
		// Given
		String id = "notificationId";

		// When
		mockMvc.perform(delete("/rest/notification/delete").param("id", id).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk()).andReturn();

		// Then
		verify(deleteNotificationUseCase).delete(id);
	}
}
