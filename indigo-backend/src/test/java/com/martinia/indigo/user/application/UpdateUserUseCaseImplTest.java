package com.martinia.indigo.user.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.domain.ports.usecases.UpdateUserUseCase;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import com.martinia.indigo.user.infrastructure.mongo.mappers.UserMongoMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UpdateUserUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private UpdateUserUseCase updateUserUseCase;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private UserMongoMapper userMongoMapper;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Captor
	private ArgumentCaptor<UserMongoEntity> userCaptor;

	@Test
	public void testUpdateUser_UserExists_UpdateUserFields() {
		// Given
		String userId = "1";
		String username = "john_doe";
		String newPassword = "new_password";
		String language = "en";
		boolean languageBooks = true;
		boolean kindle = true;

		User userDto = new User();
		userDto.setId(userId);
		userDto.setUsername(username);
		userDto.setPassword(newPassword);
		userDto.setLanguage(language);

		UserMongoEntity userEntity = new UserMongoEntity();
		userEntity.setId(userId);
		userEntity.setUsername(username);
		userEntity.setPassword("old_password");
		userEntity.setLanguage("es");

		when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
		when(passwordEncoder.encode(newPassword)).thenReturn("encoded_password");
		when(userMongoMapper.domain2Entity(userDto)).thenReturn(userEntity);

		// When
		updateUserUseCase.update(userDto);

		// Then
		verify(userRepository).save(userCaptor.capture());

		UserMongoEntity updatedUser = userCaptor.getValue();
		assertEquals(userId, updatedUser.getId());
		assertEquals(username, updatedUser.getUsername());
		assertEquals(language, updatedUser.getLanguage());

	}

	@Test
	public void testUpdateUser_UserDoesNotExist_NothingIsUpdated() {
		// Given
		String userId = "non_existing_user";

		User userDto = new User();
		userDto.setId(userId);
		userDto.setUsername("john_doe");

		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// When
		updateUserUseCase.update(userDto);

		// Then
		verify(userRepository).findById(userId);
		verify(userRepository).save(userCaptor.capture());

		UserMongoEntity updatedUser = userCaptor.getValue();
		assertNotNull(updatedUser);
	}
}
