package com.martinia.indigo.user.infrastructure.api;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.user.infrastructure.model.UserDto;
import com.martinia.indigo.user.infrastructure.mapper.UserDtoMapper;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.usecases.FindAllUsersUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FindAllUsersControllerTest extends BaseIndigoTest {

	private MockMvc mockMvc;

	@Mock
	private FindAllUsersUseCase useCase;

	@Mock
	private UserDtoMapper mapper;

	@InjectMocks
	private FindAllUsersController controller;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	void testGetAll() throws Exception {
		// Arrange
		List<User> users = new ArrayList<>();
		users.add(new User("1", "user1", "password1", "kindle1", "role1", "language1", new ArrayList<>(), new ArrayList<>(),
				new ArrayList<>()));
		users.add(new User("2", "user2", "password2", "kindle2", "role2", "language2", new ArrayList<>(), new ArrayList<>(),
				new ArrayList<>()));
		when(useCase.findAll()).thenReturn(users);

		List<UserDto> usersDto = new ArrayList<>();
		usersDto.add(new UserDto("1", "user1", "password1", "kindle1", null, null, null));
		usersDto.add(new UserDto("2", "user2", "password2", "kindle2", null, null, null));
		when(mapper.domains2Dtos(users)).thenReturn(usersDto);

		// Act
		MvcResult mvcResult = mockMvc.perform(get("/api/user/getAll").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

		// Assert
		String responseContent = mvcResult.getResponse().getContentAsString();
		// Verificar el contenido de la respuesta, por ejemplo:
		assertEquals(
				"[{\"id\":\"1\",\"username\":\"user1\",\"password\":\"password1\",\"kindle\":\"kindle1\",\"role\":null,\"language\":null,\"languageBooks\":null},{\"id\":\"2\",\"username\":\"user2\",\"password\":\"password2\",\"kindle\":\"kindle2\",\"role\":null,\"language\":null,\"languageBooks\":null}]",
				responseContent);
	}

	@Test
	void testGetAll_EmptyList() throws Exception {
		// Arrange
		List<User> emptyList = new ArrayList<>();
		when(useCase.findAll()).thenReturn(emptyList);

		// Act
		MvcResult mvcResult = mockMvc.perform(get("/api/user/getAll").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

		// Assert
		String responseContent = mvcResult.getResponse().getContentAsString();
		// Verificar que la respuesta sea una lista vacía, por ejemplo:
		assertEquals("[]", responseContent);
	}
}