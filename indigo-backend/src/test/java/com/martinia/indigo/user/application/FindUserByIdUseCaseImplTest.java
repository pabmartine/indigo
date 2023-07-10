package com.martinia.indigo.user.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FindUserByIdUseCaseImplTest extends BaseIndigoTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private FindUserByIdUseCaseImpl useCase;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testFindById_UserExists() {
		// Arrange
		String id = "1";
		User user = new User(id, "johnDoe", "password", "kindle", "role", "language", null, null, null);
		when(userRepository.findById(id)).thenReturn(Optional.of(user));

		// Act
		Optional<User> result = useCase.findById(id);

		// Assert
		assertTrue(result.isPresent());
		assertEquals(user, result.get());
		verify(userRepository, times(1)).findById(id);
	}

	@Test
	public void testFindById_UserDoesNotExist() {
		// Arrange
		String id = "nonExistingId";
		when(userRepository.findById(id)).thenReturn(Optional.empty());

		// Act
		Optional<User> result = useCase.findById(id);

		// Assert
		assertFalse(result.isPresent());
		verify(userRepository, times(1)).findById(id);
	}

}