package com.martinia.indigo.common.login.utils;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.martinia.indigo.common.login.model.UserAuthDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JWTParserComponent {

	private String AUTH_TYPE = "Bearer";
	private String HTTP_HEADER = "Authorization";

	@Value("${jwt.criptKey}")
	private String criptKey;

	@Value("${jwt.expiration}")
	private long expirationTime;

	public void createToken(HttpServletResponse response, String username) {

		String JWT = new TokenUtils().createToken(username, criptKey,
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
					return new UserAuthDto(user);
				}
			} catch (Exception ex) {
				request.setAttribute("payload", ex.getMessage());
				log.error(ex.getMessage());
			}

		}
		return null;

	}

}
