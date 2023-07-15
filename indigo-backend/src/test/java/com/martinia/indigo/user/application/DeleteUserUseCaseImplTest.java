package com.martinia.indigo.user.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.domain.ports.usecases.DeleteUserUseCase;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeleteUserUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private DeleteUserUseCase deleteUserUseCase;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private PasswordEncoder passwordEncoder;

	@Test
	public void testDeleteUser_UserExists_DeletesUser() {
		// Given
		String userId = "1";

		UserMongoEntity userEntity = new UserMongoEntity();
		userEntity.setId(userId);

		when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

		// When
		deleteUserUseCase.delete(userId);

		// Then
		verify(userRepository).delete(userEntity);
	}

	@Test
	public void testDeleteUser_UserDoesNotExist_DoesNothing() {
		// Given
		String userId = "1";

		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// When
		deleteUserUseCase.delete(userId);

		// Then
		verify(userRepository, Mockito.never()).delete(Mockito.any());
	}
}

