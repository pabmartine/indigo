package com.martinia.indigo.metadata.infrastructure.adapters.wikipedia;

import static org.junit.jupiter.api.Assertions.*;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.ports.usecases.wikipedia.FindWikipediaAuthorUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class FindWikipediaAuthorAdapterTest extends BaseIndigoTest {

	@MockBean
	private FindWikipediaAuthorUseCase findWikipediaAuthorUseCase;

	@Resource
	private FindWikipediaAuthorAdapter findWikipediaAuthorAdapter;

	@Test
	public void testFindAuthor_Successful() {
		// Given
		String subject = "example_subject";
		String lang = "example_lang";
		int cont = 5;
		String[] expectedResults = new String[]{"author1", "author2"};

		when(findWikipediaAuthorUseCase.findAuthor(subject, lang, cont)).thenReturn(expectedResults);

		// When
		String[] results = findWikipediaAuthorAdapter.findAuthor(subject, lang, cont);

		// Then
		assertArrayEquals(expectedResults, results);
		// Verify the method invocation
		verify(findWikipediaAuthorUseCase, times(1)).findAuthor(subject, lang, cont);
	}
}
