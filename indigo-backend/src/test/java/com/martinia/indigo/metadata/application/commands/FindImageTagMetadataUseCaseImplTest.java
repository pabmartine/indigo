package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.BaseIndigoTest;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertNull;

class FindImageTagMetadataUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindImageTagMetadataUseCaseImpl useCase;

	@Test
	public void testFind() {
		// Given
		String term = "testTerm";

		// When
		String result = useCase.find(term);

		// Then
		assertNull(result);

	}

}