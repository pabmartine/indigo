package com.martinia.indigo.book.infrastructure.api.view;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.service.view.MarkBookAsViewUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MarkBookAsViewControllerTest extends BaseIndigoTest {

	@MockBean
	private MarkBookAsViewUseCase useCase;

	@Resource
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	void testView() throws Exception {
		// Given
		String user = "example_user";
		String book = "example_book";

		// When
		mockMvc.perform(MockMvcRequestBuilders.post("/rest/book/view").param("user", user).param("book", book))
				.andExpect(MockMvcResultMatchers.status().isOk());

		// Then
		verify(useCase, times(1)).save(any());
	}

}