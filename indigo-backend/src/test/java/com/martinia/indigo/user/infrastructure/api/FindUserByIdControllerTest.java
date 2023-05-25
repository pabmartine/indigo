package com.martinia.indigo.user.infrastructure.api;

import com.martinia.indigo.adapters.in.rest.dtos.UserDto;
import com.martinia.indigo.adapters.in.rest.mappers.UserDtoMapper;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.service.FindUserByIdUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FindUserByIdControllerTest {


	private MockMvc mockMvc;

	@Mock
	private FindUserByIdUseCase findUserByIdUseCase;

	@Mock
	private UserDtoMapper userDtoMapper;

	@InjectMocks
	private FindUserByIdController findUserByIdController;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(findUserByIdController).build();
	}

	@Test
	public void testGetById_UserExists() throws Exception {
		// Arrange
		String id = "1";
		User user = new User(id, "johnDoe", "password", "kindle", "role", "language", null, null, null);
		UserDto userDto = new UserDto(id, "johnDoe", "password", "kindle", "role", "language", null);
		when(findUserByIdUseCase.findById(id)).thenReturn(Optional.of(user));
		when(userDtoMapper.domain2Dto(user)).thenReturn(userDto);

		// Act & Assert
		mockMvc.perform(MockMvcRequestBuilders.get("/rest/user/getById").param("id", id).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(id)).andExpect(jsonPath("$.username").value("johnDoe"))
				.andExpect(jsonPath("$.kindle").value("kindle")).andExpect(jsonPath("$.role").value("role"))
				.andExpect(jsonPath("$.language").value("language"));

		verify(findUserByIdUseCase, times(1)).findById(id);
		verify(userDtoMapper, times(1)).domain2Dto(user);
	}

	@Test
	public void testGetById_UserDoesNotExist() throws Exception {
		// Arrange
		String id = "nonExistingId";
		when(findUserByIdUseCase.findById(id)).thenReturn(Optional.empty());

		// Act & Assert
		mockMvc.perform(MockMvcRequestBuilders.get("/rest/user/getById").param("id", id).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").doesNotExist());

		verify(findUserByIdUseCase, times(1)).findById(id);
		verifyNoInteractions(userDtoMapper);
	}
}