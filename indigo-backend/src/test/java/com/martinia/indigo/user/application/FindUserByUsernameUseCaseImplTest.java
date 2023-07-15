package com.martinia.indigo.user.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.domain.ports.usecases.FindUserByUsernameUseCase;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import com.martinia.indigo.user.infrastructure.mongo.mappers.UserMongoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class FindUserByUsernameUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindUserByUsernameUseCase findUserByUsernameUseCase;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private UserMongoMapper userMongoMapper;

	@Test
	public void testFindUserByUsername_UserExists_ReturnsUser() {
		// Given
		String username = "john_doe";

		UserMongoEntity userEntity = new UserMongoEntity();
		userEntity.setId("1");
		userEntity.setUsername(username);

		User userDto = new User();
		userDto.setId("1");
		userDto.setUsername(username);

		when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
		when(userMongoMapper.entity2Domain(userEntity)).thenReturn(userDto);

		// When
		Optional<User> result = findUserByUsernameUseCase.findByUsername(username);

		// Then
		assertEquals(Optional.of(userDto), result);
	}

	@Test
	public void testFindUserByUsername_UserDoesNotExist_ReturnsEmptyOptional() {
		// Given
		String username = "non_existing_user";

		when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

		// When
		Optional<User> result = findUserByUsernameUseCase.findByUsername(username);

		// Then
		assertEquals(Optional.empty(), result);
	}
}
