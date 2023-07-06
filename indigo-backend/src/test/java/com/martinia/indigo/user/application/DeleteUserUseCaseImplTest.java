package com.martinia.indigo.user.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

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
