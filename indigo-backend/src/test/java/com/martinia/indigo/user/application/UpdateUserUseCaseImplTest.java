package com.martinia.indigo.user.application;
import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.usecases.FindUserByIdUseCase;
import com.martinia.indigo.user.domain.ports.usecases.UpdateUserUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class UpdateUserUseCaseImplTest extends BaseIndigoTest {

	@Autowired
	private UpdateUserUseCase useCase;

//	@MockBean
//	private UserMongoRepository userMongoRepository;

	@MockBean
	private FindUserByIdUseCase findUserByIdUseCase;

	@MockBean
	private PasswordEncoder passwordEncoder;

	@Test
	void testUpdate_PasswordChanged() {
		// Arrange
		String userId = "1";
		String username = "new_username";
		String password = "new_password";
		String language = "new_language";
		User existingUser = new User(userId, "old_username", "old_password", "kindle", "role", "old_language",
				null, null, null);
		User updatedUser = new User(userId, username, password, "kindle", "role", language,
				null, null, null);

		when(findUserByIdUseCase.findById(userId)).thenReturn(Optional.of(existingUser));
		when(passwordEncoder.encode(password)).thenReturn("encoded_password");

		// Act
		useCase.update(updatedUser);

		// Assert
//		verify(userRepository).save(existingUser);
		assertEquals(username, existingUser.getUsername());
		assertEquals(language, existingUser.getLanguage());
		assertEquals("encoded_password", existingUser.getPassword());
	}

	@Test
	void testUpdate_PasswordNotChanged() {
		// Arrange
		String userId = "1";
		String username = "new_username";
		String password = "old_password";
		String language = "new_language";
		User existingUser = new User(userId, "old_username", "old_password", "kindle", "role", "old_language",
				null, null, null);
		User updatedUser = new User(userId, username, password, "kindle", "role", language,
				null, null, null);

		when(findUserByIdUseCase.findById(userId)).thenReturn(Optional.of(existingUser));

		// Act
		useCase.update(updatedUser);

		// Assert
//		verify(userRepository).save(existingUser);
		assertEquals(username, existingUser.getUsername());
		assertEquals(language, existingUser.getLanguage());
		assertEquals(password, existingUser.getPassword());
	}

	@Test
	void testUpdate_UserNotFound() {
		// Arrange
		String userId = "1";
		User updatedUser = new User(userId, "new_username", "new_password", "kindle", "role", "new_language",
				null, null, null);

		when(findUserByIdUseCase.findById(userId)).thenReturn(Optional.empty());

		// Act
		useCase.update(updatedUser);

		// Assert
//		verify(userRepository, times(0)).save(updatedUser);
	}
}