package com.martinia.indigo.metadata.application.wikipedia;

import com.martinia.indigo.common.util.DataUtils;
import com.martinia.indigo.metadata.domain.ports.adapters.wikipedia.FindWikipediaAuthorInfoPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindWikipediaAuthorUseCaseImplTest {

	@Mock
	private DataUtils dataUtils;
	@Mock
	private FindWikipediaAuthorInfoPort infoPort;
	private FindWikipediaAuthorUseCaseImpl useCase;

	@BeforeEach
	void setUp() {
		useCase = new FindWikipediaAuthorUseCaseImpl();
		ReflectionTestUtils.setField(useCase, "endpoint", "https://$lang.example/$subject");
		ReflectionTestUtils.setField(useCase, "dataUtils", dataUtils);
		ReflectionTestUtils.setField(useCase, "findWikipediaAuthorInfoPort", infoPort);
	}

	@Test
	void doesNotAcceptAnUnrelatedAuthorSharingOnlySomeTerms() {
		when(dataUtils.getData("https://es.example/A%20P%20Hernandez"))
				.thenReturn("{\"query\":{\"search\":[{\"title\":\"Miguel Hernández\"}]}}");

		assertNull(useCase.findAuthor("A. P. Hernández", "es", 0));
		verify(infoPort, never()).getAuthorInfo("Miguel Hernández", "es");
	}

	@Test
	void acceptsAnExactAuthorName() {
		String[] expected = { "bio", "image", "WIKIPEDIA" };
		when(dataUtils.getData("https://es.example/Abraham%20Merritt"))
				.thenReturn("{\"query\":{\"search\":[{\"title\":\"Abraham Merritt\"}]}}");
		when(infoPort.getAuthorInfo("Abraham Merritt", "es")).thenReturn(expected);

		assertArrayEquals(expected, useCase.findAuthor("Abraham Merritt", "es", 0));
	}
}
