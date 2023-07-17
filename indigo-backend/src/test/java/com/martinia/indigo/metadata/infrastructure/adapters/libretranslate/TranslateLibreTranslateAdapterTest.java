package com.martinia.indigo.metadata.infrastructure.adapters.libretranslate;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.ports.usecases.libretranslate.TranslateLibreTranslateUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TranslateLibreTranslateAdapterTest extends BaseIndigoTest {

	@MockBean
	private TranslateLibreTranslateUseCase useCase;

	@Resource
	private TranslateLibreTranslateAdapter adapter;

	@Test
	void translate_ShouldReturnTranslatedText() {
		String text = "Hello, world!";
		String targetLanguage = "es";
		String expectedTranslation = "¡Hola, mundo!";

		when(useCase.translate(text, targetLanguage)).thenReturn(expectedTranslation);

		String translatedText = adapter.translate(text, targetLanguage);

		assertEquals(expectedTranslation, translatedText);
		verify(useCase).translate(text, targetLanguage);
	}
}
