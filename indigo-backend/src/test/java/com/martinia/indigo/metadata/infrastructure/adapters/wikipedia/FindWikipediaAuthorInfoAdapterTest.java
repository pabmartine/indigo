package com.martinia.indigo.metadata.infrastructure.adapters.wikipedia;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.ports.usecases.wikipedia.FindWikipediaAuthorInfoUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FindWikipediaAuthorInfoAdapterTest extends BaseIndigoTest {

	@MockBean
	private FindWikipediaAuthorInfoUseCase useCase;

	@Resource
	private FindWikipediaAuthorInfoAdapter adapter;

	@Test
	void getAuthorInfo_ShouldReturnAuthorInfo() {
		String subject = "John Doe";
		String lang = "en";
		String[] expectedAuthorInfo = { "John Doe", "Born: January 1, 1980", "Nationality: American" };

		when(useCase.getAuthorInfo(subject, lang)).thenReturn(expectedAuthorInfo);

		String[] authorInfo = adapter.getAuthorInfo(subject, lang);

		assertArrayEquals(expectedAuthorInfo, authorInfo);
		verify(useCase).getAuthorInfo(subject, lang);
	}
}
