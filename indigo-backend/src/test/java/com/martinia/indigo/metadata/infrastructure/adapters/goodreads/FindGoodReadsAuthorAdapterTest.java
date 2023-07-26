package com.martinia.indigo.metadata.infrastructure.adapters.goodreads;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.ports.usecases.goodreads.FindGoodReadsAuthorUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.when;

class FindGoodReadsAuthorAdapterTest extends BaseIndigoTest {

	@MockBean
	private FindGoodReadsAuthorUseCase findGoodReadsAuthorUseCase;

	@Resource
	private FindGoodReadsAuthorAdapter findGoodReadsAuthorAdapter;

	@Test
	public void testFindAuthor() {
		// Given
		String key = "example_key";
		String subject = "example_subject";
		String[] expectedResult = { "author1", "author2", "author3" };

		when(findGoodReadsAuthorUseCase.findAuthor(key, subject)).thenReturn(expectedResult);

		// When
		String[] result = findGoodReadsAuthorAdapter.findAuthor(key, subject);

		// Then
		assertArrayEquals(expectedResult, result);
	}

}