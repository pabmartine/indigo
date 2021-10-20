package com.martinia.indigo.adapters.in.login.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.martinia.indigo.adapters.in.login.utils.JWTParserComponent;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

	private JWTParserComponent jwtParserComponent;

	public JWTAuthorizationFilter(AuthenticationManager authenticationManager, JWTParserComponent jwtParserComponent) {
		super(authenticationManager);
		this.jwtParserComponent = jwtParserComponent;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		Authentication authentication = jwtParserComponent.readToken(request);

		if (authentication != null) {
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} 

		chain.doFilter(request, response);
	}

}
