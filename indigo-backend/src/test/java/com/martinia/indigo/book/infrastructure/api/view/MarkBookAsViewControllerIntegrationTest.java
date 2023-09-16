package com.martinia.indigo.book.infrastructure.api.view;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.common.infrastructure.mongo.entities.ViewMongoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MarkBookAsViewControllerIntegrationTest extends BaseIndigoTest {

	@Resource
	private MockMvc mockMvc;

	@Test
	@WithMockUser
	void testView() throws Exception {
		// Given
		String book = "book";
		String user = "user";

		// When
		final ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/book/view").param("user", user).param("book", book));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());

		ViewMongoEntity view = viewRepository.findAll().stream().findAny().get();
		assertEquals(book, view.getBook());
		assertEquals(user, view.getUser());
		assertNotNull(view.getViewDate());
	}

}