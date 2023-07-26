package com.martinia.indigo.metadata.infrastructure.adapters.goodreads;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.ports.usecases.goodreads.FindGoodReadsBookUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class FindGoodReadsBookAdapterTest extends BaseIndigoTest {

	@MockBean
	private FindGoodReadsBookUseCase findGoodReadsBookUseCase;

	@Resource
	private FindGoodReadsBookAdapter findGoodReadsBookAdapter;

	@Test
	public void testFindBook_Successful() {
		// Given
		String key = "example_key";
		String title = "example_title";
		List<String> authors = Arrays.asList("author1", "author2");
		boolean withAuthor = true;
		String[] expectedResults = { "result1", "result2" };

		when(findGoodReadsBookUseCase.findBook(key, title, authors, withAuthor)).thenReturn(expectedResults);

		// When
		String[] results = findGoodReadsBookAdapter.findBook(key, title, authors, withAuthor);

		// Then
		assertArrayEquals(expectedResults, results);
		// Verify the method invocation
		verify(findGoodReadsBookUseCase, times(1)).findBook(key, title, authors, withAuthor);
	}
}