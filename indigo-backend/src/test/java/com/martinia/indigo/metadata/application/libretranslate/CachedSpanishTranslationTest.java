package com.martinia.indigo.metadata.application.libretranslate;

import com.martinia.indigo.metadata.domain.ports.adapters.libretranslate.TranslateLibreTranslatePort;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CachedSpanishTranslationTest {
    @Test void languagePolicyAvoidsUnnecessaryCalls() {
        var mongo = mock(MongoTemplate.class);
        var translator = mock(TranslateLibreTranslatePort.class);
        var detector = mock(com.martinia.indigo.metadata.domain.ports.adapters.libretranslate.DetectLibreTranslatePort.class);
        var service = new CachedSpanishTranslation();
        ReflectionTestUtils.setField(service, "mongo", mongo);
        ReflectionTestUtils.setField(service, "translator", Optional.of(translator));
        ReflectionTestUtils.setField(service, "detector", Optional.of(detector));
        assertEquals("Hola", service.translate("Hola", "es-ES").text());
        verifyNoInteractions(mongo, translator, detector);
        when(detector.detect("Texto español")).thenReturn("es");
        assertEquals("Texto español", service.translate("Texto español"));
        verify(detector).detect("Texto español");
        verifyNoInteractions(translator);
        when(translator.translate("Writer", "es")).thenReturn("Escritor");
        assertEquals("Escritor", service.translate("Writer", "en").text());
        verify(detector, never()).detect("Writer");
        when(detector.detect("Unknown English")).thenReturn("en");
        when(translator.translate("Unknown English", "es")).thenReturn("Inglés");
        assertEquals("Inglés", service.translate("Unknown English"));
        verify(detector).detect("Unknown English");
        verify(translator).translate("Unknown English", "es");
        assertNull(service.translate("Undetected"));
        verify(translator, never()).translate(eq("Undetected"), anyString());
        assertEquals("", service.translate("", null).text());
        verify(detector, never()).detect("");
    }
    @Test void cachedTranslationAvoidsRemoteCall() {
        var mongo = mock(MongoTemplate.class);
        var translator = mock(TranslateLibreTranslatePort.class);
        var service = new CachedSpanishTranslation();
        ReflectionTestUtils.setField(service, "mongo", mongo);
        ReflectionTestUtils.setField(service, "translator", Optional.of(translator));
        when(mongo.findById(anyString(), eq(Document.class), eq("metadataTranslations"))).thenReturn(new Document("translation", "Escritor"));
        assertEquals("Escritor", service.translate("Writer"));
        verifyNoInteractions(translator);
    }
    @Test void failedTranslationIsNotCached() {
        var mongo = mock(MongoTemplate.class);
        var translator = mock(TranslateLibreTranslatePort.class);
        var service = new CachedSpanishTranslation();
        ReflectionTestUtils.setField(service, "mongo", mongo);
        ReflectionTestUtils.setField(service, "translator", Optional.of(translator));
        assertEquals("Writer", service.translate("Writer", "en").text());
        verify(translator).translate("Writer", "es");
        verify(mongo, never()).save(any(), anyString());
    }
}
