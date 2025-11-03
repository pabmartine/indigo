package com.martinia.indigo.common.error.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class Http401UnauthorizedEntryPointTest {

    @InjectMocks
    private Http401UnauthorizedEntryPoint entryPoint;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private AuthenticationException authException;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        authException = new BadCredentialsException("Invalid credentials");
    }

    @Test
    void commence_WithAuthenticationException_ShouldReturn401WithJsonResponse() throws Exception {
        // Given
        request.setAttribute("payload", "test-payload");

        // When
        entryPoint.commence(request, response, authException);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);

        // Parse the JSON response
        String responseContent = response.getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = mapper.readValue(responseContent, Map.class);

        assertThat(responseBody).containsEntry("code", 401);
        assertThat(responseBody).containsEntry("payload", "test-payload");
    }

    @Test
    void commence_WithoutPayloadAttribute_ShouldReturnNullPayload() throws Exception {
        // Given - no payload attribute set

        // When
        entryPoint.commence(request, response, authException);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);

        // Parse the JSON response
        String responseContent = response.getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = mapper.readValue(responseContent, Map.class);

        assertThat(responseBody).containsEntry("code", 401);
        assertThat(responseBody).containsEntry("payload", null);
    }

    @Test
    void commence_WithDifferentExceptionTypes_ShouldHandleCorrectly() throws Exception {
        // Given
        AuthenticationException tokenExpired = new BadCredentialsException("Token expired");
        request.setAttribute("payload", "expired-token-payload");

        // When
        entryPoint.commence(request, response, tokenExpired);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);

        String responseContent = response.getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = mapper.readValue(responseContent, Map.class);

        assertThat(responseBody).containsEntry("code", 401);
        assertThat(responseBody).containsEntry("payload", "expired-token-payload");
    }

    @Test
    void commence_WithComplexPayload_ShouldSerializeCorrectly() throws Exception {
        // Given - Complex object as payload
        Map<String, Object> complexPayload = Map.of(
            "error", "authentication_failed",
            "timestamp", System.currentTimeMillis(),
            "details", Map.of("reason", "invalid_token")
        );
        request.setAttribute("payload", complexPayload);

        // When
        entryPoint.commence(request, response, authException);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);

        String responseContent = response.getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = mapper.readValue(responseContent, Map.class);

        assertThat(responseBody).containsEntry("code", 401);
        @SuppressWarnings("unchecked")
        Map<String, Object> payloadInResponse = (Map<String, Object>) responseBody.get("payload");
        assertThat(payloadInResponse).containsEntry("error", "authentication_failed");
        assertThat(payloadInResponse).containsKey("timestamp");
        assertThat(payloadInResponse).containsKey("details");
    }

    @Test
    void commence_WithNullException_ShouldStillWork() throws Exception {
        // Given
        request.setAttribute("payload", "null-exception-test");

        // When
        entryPoint.commence(request, response, null);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);

        String responseContent = response.getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = mapper.readValue(responseContent, Map.class);

        assertThat(responseBody).containsEntry("code", 401);
        assertThat(responseBody).containsEntry("payload", "null-exception-test");
    }
}