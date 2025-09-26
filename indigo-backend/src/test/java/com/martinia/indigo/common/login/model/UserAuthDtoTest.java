package com.martinia.indigo.common.login.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UserAuthDtoTest {

    @Test
    void testUserAuthDto() {
        UserAuthDto userAuthDto = new UserAuthDto("testUser");

        assertEquals("testUser", userAuthDto.getName());
        assertTrue(userAuthDto.isAuthenticated());
        assertNull(userAuthDto.getAuthorities());
        assertNull(userAuthDto.getCredentials());
        assertNull(userAuthDto.getDetails());
        assertNull(userAuthDto.getPrincipal());
    }
}
