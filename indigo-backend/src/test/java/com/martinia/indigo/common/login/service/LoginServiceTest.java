package com.martinia.indigo.common.login.service;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.domain.ports.usecases.FindUserByUsernameUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.annotation.Resource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginServiceTest extends BaseIndigoTest {

	@MockBean
	private FindUserByUsernameUseCase findUserByUsernameUseCase;

	@Resource
	private LoginService loginService;

	@Test
	void loadUserByUsername_WithExistingUser_ShouldReturnUserDetails() {
		String username = "testUser";
		User user = new User();
		user.setUsername(username);
		user.setPassword("password");
		user.setRole("ROLE_USER");

		when(findUserByUsernameUseCase.findByUsername(username)).thenReturn(Optional.of(user));

		UserDetails userDetails = loginService.loadUserByUsername(username);

		assertNotNull(userDetails);
		assertEquals(username, userDetails.getUsername());
		assertEquals(user.getPassword(), userDetails.getPassword());
		assertTrue(userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals(user.getRole())));

		verify(findUserByUsernameUseCase).findByUsername(username);
	}

	@Test
	void loadUserByUsername_WithNonExistingUser_ShouldThrowUsernameNotFoundException() {
		String username = "nonExistingUser";

		when(findUserByUsernameUseCase.findByUsername(username)).thenReturn(Optional.empty());

		assertThrows(UsernameNotFoundException.class, () -> loginService.loadUserByUsername(username));

		verify(findUserByUsernameUseCase).findByUsername(username);
	}
}
