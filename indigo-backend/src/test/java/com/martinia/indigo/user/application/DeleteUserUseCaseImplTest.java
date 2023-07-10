package com.martinia.indigo.user.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

public class DeleteUserUseCaseImplTest extends BaseIndigoTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private DeleteUserUseCaseImpl deleteUserUseCase;

	public DeleteUserUseCaseImplTest() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testDeleteUser() {
		String userId = "12345";

		deleteUserUseCase.delete(userId);

		verify(userRepository).delete(userId);
	}
}
