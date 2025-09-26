package com.martinia.indigo.common.login.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserLoginDtoTest {

    @Test
    @DisplayName("Should set and get username and password")
    void shouldSetAndGetUsernameAndPassword() {
        // Given
        UserLoginDto userLoginDto = new UserLoginDto();
        String username = "testuser";
        String password = "testpassword";

        // When
        userLoginDto.setUsername(username);
        userLoginDto.setPassword(password);

        // Then
        assertEquals(username, userLoginDto.getUsername());
        assertEquals(password, userLoginDto.getPassword());
    }
}
