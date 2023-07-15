package com.martinia.indigo.book.application.resource;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.usecases.resource.ObtainBookByPathUseCase;
import com.martinia.indigo.common.util.UtilComponent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class ObtainBookByPathUseCaseImplTest extends BaseIndigoTest {

	@javax.annotation.Resource
	private ObtainBookByPathUseCase obtainBookByPathUseCase;

	@MockBean
	private UtilComponent utilComponent;

	@Test
	public void testGetEpub_ReturnsEpubResource() {
		// Given
		String path = "path/to/book.epub";
		Resource epubResource = Mockito.mock(Resource.class);

		when(utilComponent.getEpub("path/to/book.epub")).thenReturn(epubResource);

		// When
		Resource result = obtainBookByPathUseCase.getEpub(path);

		// Then
		assertEquals(epubResource, result);
	}
}
