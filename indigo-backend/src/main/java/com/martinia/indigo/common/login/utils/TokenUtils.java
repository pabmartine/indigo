package com.martinia.indigo.common.login.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

public class TokenUtils {

    private SecretKey getSigningKey(String key) {
        return Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(String user, String key, Date expiration) {
        return Jwts.builder()
                .setSubject(user)
                .setExpiration(expiration)
                .signWith(getSigningKey(key), SignatureAlgorithm.HS256)
                .compact();
    }

    public String readToken(String token, String key) {
        return Jwts.parser()
                .setSigningKey(getSigningKey(key))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
