package com.martinia.indigo.common.login.filters;

import com.martinia.indigo.common.login.utils.JWTParserComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JWTAuthenticationFilterTest {

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JWTParserComponent jwtParserComponent;

	private JWTAuthenticationFilter authenticationFilter;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		authenticationFilter = new JWTAuthenticationFilter("/login", authenticationManager, jwtParserComponent);
	}

	@Test
	void successfulAuthentication_WithAuthentication_ShouldCreateTokenAndAddHeader() throws IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);
		Authentication authentication = mock(Authentication.class);

		when(authentication.getName()).thenReturn("username");

		authenticationFilter.successfulAuthentication(request, response, chain, authentication);

		verify(jwtParserComponent).createToken(response, "username");
	}
}


