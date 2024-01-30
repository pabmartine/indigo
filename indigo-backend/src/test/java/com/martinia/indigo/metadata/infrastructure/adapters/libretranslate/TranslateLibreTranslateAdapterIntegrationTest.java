package com.martinia.indigo.metadata.infrastructure.adapters.libretranslate;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.ports.adapters.libretranslate.TranslateLibreTranslatePort;
import com.martinia.indigo.metadata.domain.ports.usecases.libretranslate.TranslateLibreTranslateUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TranslateLibreTranslateAdapterIntegrationTest extends BaseIndigoIntegrationTest {


	@Resource
	private TranslateLibreTranslatePort translateLibreTranslatePort;

	@Test
	void translateLibreTranslateOK() {
		//given
		String text = "Hello, world!";
		String targetLanguage = "es";
		String expectedTranslation = "¡Hola, mundo!";

		final LinkedHashMap linkedHashMap = new LinkedHashMap();
		linkedHashMap.put("translatedText", "¡Hola, mundo!");
		Mockito.when(restTemplate.postForObject(anyString(), any(), any())).thenReturn(linkedHashMap);

		//when
		String translatedText = translateLibreTranslatePort.translate(text, targetLanguage);

		//then
		assertEquals(expectedTranslation, translatedText);
	}
}
