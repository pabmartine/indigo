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
	void rejectsAdditionalNamesAndAmbiguousQualifiedTitles() {
		when(dataUtils.getData("https://en.example/John%20Smith"))
				.thenReturn("{\"query\":{\"search\":[{\"title\":\"John Smith Jr.\"},{\"title\":\"John Smith (writer)\"},{\"title\":\"John Smith (historian)\"}]}}");
		assertNull(useCase.findAuthor("John Smith", "en", 0));
		org.mockito.Mockito.verifyNoInteractions(infoPort);
	}

	@Test
	void prefersExactMatchOverQualifiedTitleAndDoesNotReplaceAuthorWithSuggestion() {
		when(dataUtils.getData("https://en.example/Donald%20Honig"))
				.thenReturn("{\"query\":{\"search\":[{\"title\":\"Donald Honig (writer)\"},{\"title\":\"Donald Honig\"}],\"searchinfo\":{\"suggestion\":\"donald honing\"}}}");
		useCase.findAuthor("Donald Honig", "en", 0);
		verify(infoPort).getAuthorInfo("Donald Honig", "en");
		verify(infoPort, never()).getAuthorInfo("Donald Honig (writer)", "en");
	}

	@Test
	void preservesUnicodeNamesInEncodedSearchAndMatching() {
		when(dataUtils.getData("https://en.example/%E9%AD%AF%E8%BF%85"))
				.thenReturn("{\"query\":{\"search\":[{\"title\":\"魯迅\"}]}}");
		useCase.findAuthor("魯迅", "en", 0);
		verify(infoPort).getAuthorInfo("魯迅", "en");
	}

	@Test
	void photoOnlySearchPassesTheFieldRequirementToTheDetailsLookup() {
		when(dataUtils.getData("https://en.example/Donald%20Honig"))
				.thenReturn("{\"query\":{\"search\":[{\"title\":\"Donald Honig\"}]}}");
		String[] expected = {null, "photo", "WIKIPEDIA"};
		when(infoPort.getAuthorInfo("Donald Honig", "en", false)).thenReturn(expected);
		assertArrayEquals(expected, useCase.findAuthor("Donald Honig", "en", 0, false));
		verify(infoPort, never()).getAuthorInfo("Donald Honig", "en");
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
