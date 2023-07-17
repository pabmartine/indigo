package com.martinia.indigo.metadata.infrastructure.adapters.libretranslate;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.ports.usecases.libretranslate.DetectLibreTranslateUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DetectLibreTranslateAdapterTest extends BaseIndigoTest {

	@MockBean
	private DetectLibreTranslateUseCase useCase;

	@Resource
	private DetectLibreTranslateAdapter adapter;

	@Test
	void detect_ShouldReturnDetectedLanguage() {
		String text = "Hello, world!";
		String expectedLanguage = "en";

		when(useCase.detect(text)).thenReturn(expectedLanguage);

		String detectedLanguage = adapter.detect(text);

		assertEquals(expectedLanguage, detectedLanguage);
		verify(useCase).detect(text);
	}
}
