package com.martinia.indigo.adapters.in.login.utils;

import java.util.Base64;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class TokenUtils {

	public String createToken(String usuario, String key, Date expiration) {

		String JWT = Jwts.builder().setSubject(usuario).setExpiration(expiration)
				.signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString((key.getBytes()))).compact();

		return JWT;

	}

	public String readToken(String token, String key) {
		String usuario = Jwts.parser().setSigningKey(Base64.getEncoder().encodeToString((key.getBytes())))
				.parseClaimsJws(token).getBody().getSubject();
		return usuario;
	}

}
