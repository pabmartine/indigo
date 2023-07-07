package com.martinia.indigo.common.login.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Base64;
import java.util.Date;

public class TokenUtils {

    public String createToken(String user, String key, Date expiration) {

        return Jwts.builder().setSubject(user).setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString((key.getBytes()))).compact();


    }

    public String readToken(String token, String key) {
        return Jwts.parser().setSigningKey(Base64.getEncoder().encodeToString((key.getBytes())))
                .parseClaimsJws(token).getBody().getSubject();

    }

}
