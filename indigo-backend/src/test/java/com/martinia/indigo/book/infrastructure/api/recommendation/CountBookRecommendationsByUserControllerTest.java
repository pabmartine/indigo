package com.martinia.indigo.book.infrastructure.api.recommendation;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.service.recommendation.CountBookRecommendationsByUserUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.annotation.Resource;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CountBookRecommendationsByUserControllerTest extends BaseIndigoTest {

	@MockBean
	private CountBookRecommendationsByUserUseCase useCase;

	@Resource
	private MockMvc mockMvc;

	@Test
	void testCountBookRecommendationsByUser() throws Exception {
		// Given
		String user = "example_user";
		when(useCase.countRecommendationsByUser(user)).thenReturn(5L);

		// When
		mockMvc.perform(MockMvcRequestBuilders.get("/rest/book/recommendations/user/count").param("user", user))
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$").value(5));

		// Then
		verify(useCase, times(1)).countRecommendationsByUser(user);
	}
}