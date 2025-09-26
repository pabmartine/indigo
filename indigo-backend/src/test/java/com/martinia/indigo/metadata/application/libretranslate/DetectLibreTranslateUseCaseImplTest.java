package com.martinia.indigo.metadata.application.libretranslate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DetectLibreTranslateUseCaseImplTest {

	@Mock
	private RestTemplate restTemplate;

	@InjectMocks
	private DetectLibreTranslateUseCaseImpl detectLibreTranslateUseCase;

	private static final String ENDPOINT = "http://localhost:5000";

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(detectLibreTranslateUseCase, "endpoint", ENDPOINT);
	}

	@Test
	void detect_WhenSuccessfulResponse_ShouldReturnLanguageCode() {
		String text = "Hello world";
		Map<String, Object> languageMap = new LinkedHashMap<>();
		languageMap.put("language", "en");
		languageMap.put("confidence", 0.99);

		List<Map<String, Object>> response = new ArrayList<>();
		response.add(languageMap);

		when(restTemplate.postForObject(eq(ENDPOINT + "/detect"), any(MultiValueMap.class), eq(Object.class)))
				.thenReturn(response);

		String result = detectLibreTranslateUseCase.detect(text);

		assertThat(result).isEqualTo("en");
	}

	@Test
	void detect_WhenMultipleLanguagesDetected_ShouldReturnFirstLanguage() {
		String text = "Mixed text";
		Map<String, Object> firstLanguageMap = new LinkedHashMap<>();
		firstLanguageMap.put("language", "en");
		firstLanguageMap.put("confidence", 0.75);

		Map<String, Object> secondLanguageMap = new LinkedHashMap<>();
		secondLanguageMap.put("language", "es");
		secondLanguageMap.put("confidence", 0.65);

		List<Map<String, Object>> response = new ArrayList<>();
		response.add(firstLanguageMap);
		response.add(secondLanguageMap);

		when(restTemplate.postForObject(eq(ENDPOINT + "/detect"), any(MultiValueMap.class), eq(Object.class)))
				.thenReturn(response);

		String result = detectLibreTranslateUseCase.detect(text);

		assertThat(result).isEqualTo("en");
	}

	@Test
	void detect_WhenRestTemplateThrowsException_ShouldReturnNull() {
		String text = "Test text";

		when(restTemplate.postForObject(eq(ENDPOINT + "/detect"), any(MultiValueMap.class), eq(Object.class)))
				.thenThrow(new RestClientException("Connection error"));

		String result = detectLibreTranslateUseCase.detect(text);

		assertThat(result).isNull();
	}

	@Test
	void detect_WhenEmptyResponse_ShouldReturnNull() {
		String text = "Test text";
		List<Map<String, Object>> emptyResponse = new ArrayList<>();

		when(restTemplate.postForObject(eq(ENDPOINT + "/detect"), any(MultiValueMap.class), eq(Object.class)))
				.thenReturn(emptyResponse);

		String result = detectLibreTranslateUseCase.detect(text);

		assertThat(result).isNull();
	}

	@Test
	void detect_WhenNullResponse_ShouldReturnNull() {
		String text = "Test text";

		when(restTemplate.postForObject(eq(ENDPOINT + "/detect"), any(MultiValueMap.class), eq(Object.class)))
				.thenReturn(null);

		String result = detectLibreTranslateUseCase.detect(text);

		assertThat(result).isNull();
	}

	@Test
	void detect_WhenSpanishText_ShouldReturnSpanishCode() {
		String text = "Hola mundo";
		Map<String, Object> languageMap = new LinkedHashMap<>();
		languageMap.put("language", "es");
		languageMap.put("confidence", 0.95);

		List<Map<String, Object>> response = new ArrayList<>();
		response.add(languageMap);

		when(restTemplate.postForObject(eq(ENDPOINT + "/detect"), any(MultiValueMap.class), eq(Object.class)))
				.thenReturn(response);

		String result = detectLibreTranslateUseCase.detect(text);

		assertThat(result).isEqualTo("es");
	}

	@Test
	void detect_WhenMalformedResponse_ShouldReturnNull() {
		String text = "Test text";
		List<String> malformedResponse = new ArrayList<>();
		malformedResponse.add("invalid response");

		when(restTemplate.postForObject(eq(ENDPOINT + "/detect"), any(MultiValueMap.class), eq(Object.class)))
				.thenReturn(malformedResponse);

		String result = detectLibreTranslateUseCase.detect(text);

		assertThat(result).isNull();
	}
}