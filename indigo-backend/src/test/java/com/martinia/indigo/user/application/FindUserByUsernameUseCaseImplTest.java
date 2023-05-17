package com.martinia.indigo.user.application;

import com.martinia.indigo.domain.model.User;
import com.martinia.indigo.ports.out.mongo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FindUserByUsernameUseCaseImplTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private FindUserByUsernameUseCaseImpl useCase;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testFindByUsername_UserExists() {
		// Arrange
		String username = "johnDoe";
		List<String> languageBooks = Arrays.asList("Book 1", "Book 2");
		User user = new User("1", username, "password", "kindle", "role", "language", languageBooks, null, null);
		when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

		// Act
		Optional<User> result = useCase.findByUsername(username);

		// Assert
		assertTrue(result.isPresent());
		assertEquals(user, result.get());
		verify(userRepository, times(1)).findByUsername(username);
	}

	@Test
	public void testFindByUsername_UserDoesNotExist() {
		// Arrange
		String username = "nonExistingUser";
		when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

		// Act
		Optional<User> result = useCase.findByUsername(username);

		// Assert
		assertFalse(result.isPresent());
		verify(userRepository, times(1)).findByUsername(username);
	}
}