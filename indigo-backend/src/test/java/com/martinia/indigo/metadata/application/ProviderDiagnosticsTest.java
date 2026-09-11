package com.martinia.indigo.metadata.application;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;

class ProviderDiagnosticsTest {
    @Test void classifiesNestedHttpFailureWithoutPersistingSensitiveContent() {
        ProviderDiagnostics.begin();
        ProviderDiagnostics.record("AMAZON", "Reseñas", new RuntimeException("secret", new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS)));
        var details = ProviderDiagnostics.finish();
        assertEquals("RATE_LIMIT", details.get(0).getString("code"));
        assertEquals(429, details.get(0).getInteger("httpStatus"));
        assertFalse(details.toString().contains("secret"));
        assertTrue(ProviderDiagnostics.finish().isEmpty());
    }
    @Test void detectionAndTranslationHaveSeparateDiagnostics() {
        ProviderDiagnostics.begin();
        ProviderDiagnostics.record("LIBRETRANSLATE", "Detectar idioma", new java.net.ConnectException());
        ProviderDiagnostics.record("LIBRETRANSLATE", "Traducir al español", new java.net.SocketTimeoutException());
        var details = ProviderDiagnostics.finish();
        assertEquals("CONNECTION", details.get(0).getString("code"));
        assertEquals("TIMEOUT", details.get(1).getString("code"));
    }
}
