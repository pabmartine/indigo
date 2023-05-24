package com.martinia.indigo.serie.infrastructure.api;

import com.martinia.indigo.serie.domain.service.FindNumBooksBySerieUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@AutoConfigureMockMvc
public class FindNumBooksBySerieControllerTest {

	@MockBean
	private FindNumBooksBySerieUseCase useCase;

	@Autowired
	private MockMvc mockMvc;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testGetNumBooksBySerie_WithValidParams_ShouldReturnNumBooksBySerie() throws Exception {
		// Given
		List<String> languages = List.of("en", "es");
		int page = 1;
		int size = 10;
		String sort = "title";
		String order = "asc";
		Map<String, Long> data = new HashMap<>();
		data.put("Serie1", 5L);
		data.put("Serie2", 3L);
		when(useCase.getNumBooksBySerie(anyList(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(data);

		// When
		ResultActions resultActions = mockMvc.perform(
				get("/rest/serie/all").param("languages", String.join(",", languages)).param("page", String.valueOf(page))
						.param("size", String.valueOf(size)).param("sort", sort).param("order", order)
						.contentType(MediaType.APPLICATION_JSON));
		MvcResult mvcResult = resultActions.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(data.size()))
				.andExpect(jsonPath("$[0].name").exists()).andExpect(jsonPath("$[0].numBooks").exists()).andReturn();

		// Then
		// Perform additional assertions if needed
	}
}
