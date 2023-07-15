package com.martinia.indigo.user.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.domain.ports.usecases.FindUserByIdUseCase;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import com.martinia.indigo.user.infrastructure.mongo.mappers.UserMongoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class FindUserByIdUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindUserByIdUseCase findUserByIdUseCase;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private UserMongoMapper userMongoMapper;

	@Test
	public void testFindUserById_UserExists_ReturnsUser() {
		// Given
		String userId = "1";

		UserMongoEntity userEntity = new UserMongoEntity();
		userEntity.setId(userId);
		userEntity.setUsername("john_doe");

		User userDto = new User();
		userDto.setId(userId);
		userDto.setUsername("john_doe");

		when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
		when(userMongoMapper.entity2Domain(userEntity)).thenReturn(userDto);

		// When
		Optional<User> result = findUserByIdUseCase.findById(userId);

		// Then
		assertEquals(Optional.of(userDto), result);
	}

	@Test
	public void testFindUserById_UserDoesNotExist_ReturnsEmptyOptional() {
		// Given
		String userId = "non_existing_user";

		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// When
		Optional<User> result = findUserByIdUseCase.findById(userId);

		// Then
		assertEquals(Optional.empty(), result);
	}
}
