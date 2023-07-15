package com.martinia.indigo.user.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.domain.ports.usecases.FindAllUsersUseCase;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import com.martinia.indigo.user.infrastructure.mongo.mappers.UserMongoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class FindAllUsersUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindAllUsersUseCase findAllUsersUseCase;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private UserMongoMapper userMongoMapper;

	@Test
	public void testFindAllUsers_ReturnsAllUsers() {
		// Given
		UserMongoEntity user1 = new UserMongoEntity();
		user1.setId("1");
		user1.setUsername("john_doe");

		UserMongoEntity user2 = new UserMongoEntity();
		user2.setId("2");
		user2.setUsername("jane_smith");

		List<UserMongoEntity> userEntities = Arrays.asList(user1, user2);

		User user1Dto = new User();
		user1Dto.setId("1");
		user1Dto.setUsername("john_doe");

		User user2Dto = new User();
		user2Dto.setId("2");
		user2Dto.setUsername("jane_smith");

		List<User> expectedUsers = Arrays.asList(user1Dto, user2Dto);

		when(userRepository.findAll()).thenReturn(userEntities);
		when(userMongoMapper.entities2Domains(userEntities)).thenReturn(expectedUsers);

		// When
		List<User> result = findAllUsersUseCase.findAll();

		// Then
		assertEquals(expectedUsers, result);
	}
}
