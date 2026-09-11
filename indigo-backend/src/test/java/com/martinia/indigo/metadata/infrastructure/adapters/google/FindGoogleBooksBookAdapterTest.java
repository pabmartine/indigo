package com.martinia.indigo.metadata.infrastructure.adapters.google;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.ports.usecases.google.FindGoogleBooksBookUseCase;
import com.martinia.indigo.metadata.domain.model.BookMetadataResult;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class FindGoogleBooksBookAdapterTest extends BaseIndigoTest {

	@MockBean
	private FindGoogleBooksBookUseCase findGoogleBooksBookUseCase;

	@Resource
	private FindGoogleBooksBookAdapter findGoogleBooksBookAdapter;

	@Test
	public void testFindBook_Successful() {
		// Given
		String title = "example_title";
		List<String> authors = Arrays.asList("author1", "author2");
		BookMetadataResult expectedResults = BookMetadataResult.builder().ratingAverage(4F).build();

		when(findGoogleBooksBookUseCase.findBook(title, authors)).thenReturn(expectedResults);

		// When
		BookMetadataResult results = findGoogleBooksBookAdapter.findBook(title, authors);

		// Then
		assertSame(expectedResults, results);
		// Verify the method invocation
		verify(findGoogleBooksBookUseCase, times(1)).findBook(title, authors);
	}
}
