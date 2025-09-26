package com.martinia.indigo.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DataUtilsTest {

    private DataUtils dataUtils;

    @BeforeEach
    void setUp() {
        dataUtils = new DataUtils();
    }

    @Test
    void getData_WithValidUrl_ShouldReturnData() throws Exception {
        // Given
        String testUrl = "https://httpbin.org/json";

        // When
        String result = dataUtils.getData(testUrl);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
    }

    @Test
    void getData_WithInvalidUrl_ShouldThrowException() {
        // Given
        String invalidUrl = "invalid-url";

        // When & Then
        assertThatThrownBy(() -> dataUtils.getData(invalidUrl))
                .isInstanceOf(Exception.class);
    }

    @Test
    void getData_WithNonExistentUrl_ShouldThrowException() {
        // Given
        String nonExistentUrl = "https://this-domain-should-not-exist-12345.com";

        // When & Then
        assertThatThrownBy(() -> dataUtils.getData(nonExistentUrl))
                .isInstanceOf(Exception.class);
    }

    @Test
    void getData_WithHttpsUrl_ShouldWork() throws Exception {
        // Given
        String httpsUrl = "https://httpbin.org/uuid";

        // When
        String result = dataUtils.getData(httpsUrl);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).contains("uuid");
    }

    @Test
    void getData_WithHttpUrl_ShouldWork() throws Exception {
        // Given
        String httpUrl = "http://httpbin.org/ip";

        // When
        String result = dataUtils.getData(httpUrl);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).contains("origin");
    }

    @Test
    void getData_WithJsonResponse_ShouldReturnJsonString() throws Exception {
        // Given
        String jsonUrl = "https://httpbin.org/json";

        // When
        String result = dataUtils.getData(jsonUrl);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).contains("{");
        assertThat(result).contains("}");
    }

    @Test
    void getData_WithMalformedUrl_ShouldThrowException() {
        // Given
        String malformedUrl = "htp://invalid-protocol.com";

        // When & Then
        assertThatThrownBy(() -> dataUtils.getData(malformedUrl))
                .isInstanceOf(Exception.class);
    }

    @Test
    void getData_WithEmptyString_ShouldThrowException() {
        // Given
        String emptyUrl = "";

        // When & Then
        assertThatThrownBy(() -> dataUtils.getData(emptyUrl))
                .isInstanceOf(Exception.class);
    }
}