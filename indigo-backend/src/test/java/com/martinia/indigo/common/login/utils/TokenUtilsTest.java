package com.martinia.indigo.common.login.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.ExpiredJwtException;

class TokenUtilsTest {

    private TokenUtils tokenUtils;

    @BeforeEach
    void setUp() {
        tokenUtils = new TokenUtils();
    }

    @Test
    void testCreateAndReadToken() {
        String user = "testUser";
        String key = "ThisIsASecretKeyForTestingJsonWebTokens";
        Date expiration = new Date(System.currentTimeMillis() + 10000);

        String token = tokenUtils.createToken(user, key, expiration);
        String readUser = tokenUtils.readToken(token, key);

        assertEquals(user, readUser);
    }

    @Test
    void testReadExpiredToken() {
        String user = "testUser";
        String key = "ThisIsASecretKeyForTestingJsonWebTokens";
        Date expiration = new Date(System.currentTimeMillis() - 1000);

        String token = tokenUtils.createToken(user, key, expiration);

        assertThrows(ExpiredJwtException.class, () -> {
            tokenUtils.readToken(token, key);
        });
    }
}