package com.martinia.indigo.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.dto.auth.UserLoginDto;
import com.martinia.indigo.services.JWTParserService;

public class JWTAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	private JWTParserService jwtParserService;

	public JWTAuthenticationFilter(String url, AuthenticationManager manager, JWTParserService jwtParserService) {

		super(url);
		this.jwtParserService = jwtParserService;
		setAuthenticationManager(manager);

	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {

		UserLoginDto user = new ObjectMapper().readValue(request.getInputStream(), UserLoginDto.class);

		UsernamePasswordAuthenticationToken springUser = new UsernamePasswordAuthenticationToken(user.getUsername(),
				user.getPassword());
		return getAuthenticationManager().authenticate(springUser);
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication autentication) throws IOException, ServletException {

		// Add header
		String nombreUsuario = autentication.getName();
		jwtParserService.createToken(response, nombreUsuario);

	}

}
