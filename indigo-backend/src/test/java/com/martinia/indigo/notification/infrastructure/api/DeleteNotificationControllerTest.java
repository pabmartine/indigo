package com.martinia.indigo.notification.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.notification.domain.ports.usecases.DeleteNotificationUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.Resource;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class DeleteNotificationControllerTest extends BaseIndigoTest {

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
