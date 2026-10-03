package com.martinia.indigo.metadata.application.libretranslate;

import com.martinia.indigo.common.config.BaseConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class LibreTranslateHttpConfigurationTest {
    @Test
    void detectionAndTranslationUseDedicatedClientWithSharedClientStillAvailable() {
        try (var context = new AnnotationConfigApplicationContext()) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                    "flags.libretranslate=true", "metadata.libretranslate.url=http://translation.test");
            context.register(BaseConfiguration.class, TranslateLibreTranslateUseCaseImpl.class,
                    DetectLibreTranslateUseCaseImpl.class);
            context.refresh();
            RestTemplate translation = context.getBean("libreTranslateRestTemplate", RestTemplate.class);
            assertNotSame(context.getBean(RestTemplate.class), translation);
            var server = MockRestServiceServer.bindTo(translation).build();
            server.expect(requestTo("http://translation.test/translate"))
                    .andRespond(withSuccess("{\"translatedText\":\"Hola\"}", MediaType.APPLICATION_JSON));
            server.expect(requestTo("http://translation.test/detect"))
                    .andRespond(withSuccess("[{\"language\":\"en\",\"confidence\":100}]", MediaType.APPLICATION_JSON));
            assertEquals("Hola", context.getBean(TranslateLibreTranslateUseCaseImpl.class).translate("Hello", "es"));
            assertEquals("en", context.getBean(DetectLibreTranslateUseCaseImpl.class).detect("Hello"));
            server.verify();
        }
    }
}
