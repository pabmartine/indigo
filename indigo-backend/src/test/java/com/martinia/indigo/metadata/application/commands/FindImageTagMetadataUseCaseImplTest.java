package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.BaseIndigoTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

class FindImageTagMetadataUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindImageTagMetadataUseCaseImpl useCase;

	@MockBean
	private RestTemplate restTemplate;


	@Test
	public void testFind() {
		// Given
		String term = "testTerm";
		List<String> mockImages = Collections.singletonList("testImageUrl");

		when(restTemplate.getForObject(anyString(), eq(List.class))).thenReturn(mockImages);

		// When
		String result = useCase.find(term);

		// Then
		assertNotNull(result);

	}

}