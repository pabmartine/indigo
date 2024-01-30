package com.martinia.indigo.metadata.infrastructure.adapters.libretranslate;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.metadata.domain.ports.adapters.libretranslate.DetectLibreTranslatePort;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

class DetectLibreTranslateAdapterIntegrationTest extends BaseIndigoIntegrationTest {

	@Resource
	private DetectLibreTranslatePort detectLibreTranslatePort;

	@Test
	void detectLibreTranslateOk() {

		//Given
		String text = "Hello, world!";
		String expectedLanguage = "en";

		List list = new ArrayList();
		final LinkedHashMap linkedHashMap = new LinkedHashMap();
		linkedHashMap.put("language", "en");
		list.add(linkedHashMap);
		Mockito.when(restTemplate.postForObject(anyString(), any(), any())).thenReturn(list);

		String detectedLanguage = detectLibreTranslatePort.detect(text);

		assertEquals(expectedLanguage, detectedLanguage);
	}
}
