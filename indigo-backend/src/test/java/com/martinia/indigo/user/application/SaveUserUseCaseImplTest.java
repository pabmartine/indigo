package com.martinia.indigo.user.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.user.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SaveUserUseCaseImplTest extends BaseIndigoTest {

	@Autowired
	private SaveUserUseCaseImpl saveUserUseCase;

//	@MockBean
//	private UserMongoRepository userMongoRepository;

	@MockBean
	private PasswordEncoder passwordEncoder;

	@Test
	public void testSaveUser_NewUser() {
		User user = new User();
		user.setUsername("testuser");
		user.setPassword("password");

		saveUserUseCase.save(user, true);

//		verify(userRepository).save(user);
		verify(passwordEncoder).encode("password");
		// Puedes agregar más aserciones si es necesario
	}

	@Test
	public void testSaveUser_ExistingUser() {
		User user = new User();
		user.setId("123");
		user.setUsername("testuser");
		user.setPassword("password");

		saveUserUseCase.save(user, false);

//		verify(userRepository).save(user);
		// No se debe llamar a passwordEncoder.encode() en el caso de un usuario existente
		verify(passwordEncoder,times(0)).encode("password");
		// Puedes agregar más aserciones si es necesario
	}
}