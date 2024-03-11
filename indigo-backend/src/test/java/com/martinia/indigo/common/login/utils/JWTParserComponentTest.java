package com.martinia.indigo.common.login.utils;

import com.martinia.indigo.BaseIndigoTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JWTParserComponentTest extends BaseIndigoTest {

	@MockBean
	private HttpServletResponse response;

	@MockBean
	private HttpServletRequest request;

	@Resource
	private JWTParserComponent jwtParserComponent;

	@Test
	void createToken_ShouldAddTokenToResponseHeader() {
		String username = "testuser";
		jwtParserComponent.createToken(response, username);

		verify(response).addHeader(eq("Authorization"), startsWith("Bearer "));
	}

	@Test
	void readToken_WithInvalidToken_ShouldReturnNull() {
		String token = "Bearer your-invalid-token";
		when(request.getHeader("Authorization")).thenReturn(token);

		Authentication authentication = jwtParserComponent.readToken(request);

		assertEquals(null, authentication);
	}
}
