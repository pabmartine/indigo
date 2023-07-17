package com.martinia.indigo.common.login.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.martinia.indigo.common.login.model.UserLoginDto;
import com.martinia.indigo.common.login.utils.JWTParserComponent;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JWTAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	private JWTParserComponent jwtParserComponent;

	public JWTAuthenticationFilter(String url, AuthenticationManager manager, JWTParserComponent jwtParserComponent) {

		super(url);
		this.jwtParserComponent = jwtParserComponent;
		setAuthenticationManager(manager);

	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException {

		UserLoginDto user = new ObjectMapper().readValue(request.getInputStream(), UserLoginDto.class);

		UsernamePasswordAuthenticationToken springUser = new UsernamePasswordAuthenticationToken(user.getUsername(),
				user.getPassword());
		return getAuthenticationManager().authenticate(springUser);
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication autentication) {

		// Add header
		String userName = autentication.getName();
		jwtParserComponent.createToken(response, userName);

	}

}
