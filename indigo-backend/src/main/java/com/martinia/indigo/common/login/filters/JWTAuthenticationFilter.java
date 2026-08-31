package com.martinia.indigo.common.login.filters;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.martinia.indigo.common.login.model.UserLoginDto;
import com.martinia.indigo.common.login.utils.JWTParserComponent;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

public class JWTAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	private JWTParserComponent jwtParserComponent;

	public JWTAuthenticationFilter(String url, AuthenticationManager manager, JWTParserComponent jwtParserComponent) {

		super(new org.springframework.security.web.util.matcher.AntPathRequestMatcher(url, "POST"));
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
		List<String> roles = autentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.toList());
		jwtParserComponent.createToken(response, userName, roles);
		response.setStatus(HttpServletResponse.SC_OK);
	}

}
