package com.martinia.indigo.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.martinia.indigo.services.JWTParserService;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

	private JWTParserService jwtParserService;

	public JWTAuthorizationFilter(AuthenticationManager authenticationManager, JWTParserService jwtParserService) {
		super(authenticationManager);
		this.jwtParserService = jwtParserService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		Authentication authentication = jwtParserService.readToken(request);

		if (authentication != null) {
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} 

		chain.doFilter(request, response);
	}

}
