package com.martinia.indigo.metadata.infrastructure.adapters.goodreads;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.common.infrastructure.api.mappers.ReviewDtoMapper;
import com.martinia.indigo.common.infrastructure.api.model.ReviewDto;
import com.martinia.indigo.common.model.Review;
import com.martinia.indigo.metadata.domain.ports.usecases.goodreads.FindGoodReadsReviewsUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class FindGoodReadsReviewsAdapterTest extends BaseIndigoTest {

	@MockBean
	private FindGoodReadsReviewsUseCase findGoodReadsReviewsUseCase;

	@MockBean
	private ReviewDtoMapper reviewDtoMapper;

	@Resource
	private FindGoodReadsReviewsAdapter findGoodReadsReviewsAdapter;

	@Test
	public void testGetReviews_Successful() {
		// Given
		String lang = "en";
		String title = "example_title";
		List<String> authors = Arrays.asList("author1", "author2");
		List<Review> domainReviews = Arrays.asList(new Review(), new Review());
		List<ReviewDto> expectedResults = Arrays.asList(new ReviewDto(), new ReviewDto());

		when(findGoodReadsReviewsUseCase.getReviews(lang, title, authors)).thenReturn(domainReviews);
		when(reviewDtoMapper.domains2Dtos(domainReviews)).thenReturn(expectedResults);

		// When
		List<ReviewDto> results = findGoodReadsReviewsAdapter.getReviews(lang, title, authors);

		// Then
		assertEquals(expectedResults, results);
		// Verify the method invocations
		verify(findGoodReadsReviewsUseCase, times(1)).getReviews(lang, title, authors);
		verify(reviewDtoMapper, times(1)).domains2Dtos(domainReviews);
	}
}
