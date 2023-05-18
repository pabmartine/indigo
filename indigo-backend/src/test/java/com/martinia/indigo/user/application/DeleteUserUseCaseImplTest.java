package com.martinia.indigo.user.application;

import com.martinia.indigo.ports.out.mongo.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.verify;

@SpringBootTest
public class DeleteUserUseCaseImplTest {

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
