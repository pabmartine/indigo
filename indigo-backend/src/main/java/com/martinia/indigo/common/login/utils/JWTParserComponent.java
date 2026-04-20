package com.martinia.indigo.common.login.utils;

import java.util.Collections;
import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JWTParserComponent {

	private String AUTH_TYPE = "Bearer";
	private String HTTP_HEADER = "Authorization";

	@Value("${jwt.criptKey}")
	private String criptKey;

	@Value("${jwt.expiration}")
	private long expirationTime;

	public void createToken(HttpServletResponse response, String username, List<String> roles) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("roles", roles);

		String JWT = new TokenUtils().createToken(username, claims, criptKey,
				new Date(System.currentTimeMillis() + expirationTime));

		response.addHeader(HTTP_HEADER, AUTH_TYPE + " " + JWT);


	}

	public Authentication readToken(HttpServletRequest request) {

		String token = request.getHeader(HTTP_HEADER);

		if (token != null) {
			String tokenReal = token.substring(token.indexOf(" ") + 1);

			try {
				String user = new TokenUtils().readToken(tokenReal, criptKey);
				if (user != null) {
					List<String> roles = (List<String>) new TokenUtils().getClaim(tokenReal, criptKey, "roles");
					List<SimpleGrantedAuthority> authorities = roles != null 
							? roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
							: Collections.emptyList();
					return new UsernamePasswordAuthenticationToken(user, null, authorities);
				}
			} catch (Exception ex) {
				request.setAttribute("payload", ex.getMessage());
				log.error(ex.getMessage());
			}

		}
		return null;

	}

}

