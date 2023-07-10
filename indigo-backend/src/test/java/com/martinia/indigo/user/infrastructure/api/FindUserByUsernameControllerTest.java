package com.martinia.indigo.user.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.user.infrastructure.model.UserDto;
import com.martinia.indigo.user.infrastructure.mapper.UserDtoMapper;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.usecases.FindUserByUsernameUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FindUserByUsernameControllerTest extends BaseIndigoTest {

	private MockMvc mockMvc;

	@Mock
	private FindUserByUsernameUseCase findUserByUsernameUseCase;

	@Mock
	private UserDtoMapper userDtoMapper;

	@InjectMocks
	private FindUserByUsernameController findUserByUsernameController;

	private UserDto userDto;
	private User user;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(findUserByUsernameController).build();

		userDto = new UserDto();
		userDto.setId("1");
		userDto.setUsername("testUser");
		userDto.setPassword("password");
		userDto.setRole("user");
		userDto.setLanguage("English");

		user = new User();
		user.setId("1");
		user.setUsername("testUser");
		user.setPassword("password");
		user.setRole("user");
		user.setLanguage("English");
	}

	@Test
	public void testGetUserByUsername() throws Exception {
		String username = "testUser";
		when(findUserByUsernameUseCase.findByUsername(username)).thenReturn(Optional.of(user));
		when(userDtoMapper.domain2Dto(user)).thenReturn(userDto);

		MvcResult result = mockMvc.perform(get("/rest/user/get").param("username", username).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

		String responseContent = result.getResponse().getContentAsString();
		UserDto responseDto = new ObjectMapper().readValue(responseContent, UserDto.class);

		assertContentEquals(userDto, responseDto);
	}

	@Test
	public void testGetUserByUsername_UserNotFound() throws Exception {
		String username = "nonExistentUser";
		when(findUserByUsernameUseCase.findByUsername(username)).thenReturn(Optional.empty());

		mockMvc.perform(get("/rest/user/get").param("username", username).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().string(""));
	}

	public static void assertContentEquals(Object expected, Object actual, String... excludedFields) throws IllegalAccessException {
		Class<?> clazz = expected.getClass();
		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields) {
			field.setAccessible(true);
			String fieldName = field.getName();
			boolean isExcluded = false;

			if (excludedFields != null) {
				for (String excludedField : excludedFields) {
					if (fieldName.equals(excludedField)) {
						isExcluded = true;
						break;
					}
				}
			}

			if (isExcluded) {
				continue; // Saltar atributo excluido
			}

			Object expectedValue = field.get(expected);
			Object actualValue = field.get(actual);

			assertEquals(expectedValue, actualValue);
		}
	}
}