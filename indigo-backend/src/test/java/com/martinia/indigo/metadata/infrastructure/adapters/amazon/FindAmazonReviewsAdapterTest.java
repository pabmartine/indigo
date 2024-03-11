package com.martinia.indigo.metadata.infrastructure.adapters.amazon;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.common.infrastructure.api.mappers.ReviewDtoMapper;
import com.martinia.indigo.common.infrastructure.api.model.ReviewDto;
import com.martinia.indigo.common.domain.model.Review;
import com.martinia.indigo.metadata.domain.ports.usecases.amazon.FindAmazonReviewsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FindAmazonReviewsAdapterTest extends BaseIndigoTest {

	@MockBean
	private FindAmazonReviewsUseCase useCase;

	@MockBean
	private ReviewDtoMapper mapper;

	@Resource
	private FindAmazonReviewsAdapter adapter;

	@Test
	void getReviews_ShouldReturnReviewDtos() {
		String title = "Test Book";
		List<String> authors = Arrays.asList("Author 1", "Author 2");

		List<Review> domainReviews = Arrays.asList(new Review(), new Review());
		List<ReviewDto> expectedReviewDtos = Arrays.asList(new ReviewDto(), new ReviewDto());

		when(useCase.getReviews(title, authors)).thenReturn(domainReviews);
		when(mapper.domains2Dtos(domainReviews)).thenReturn(expectedReviewDtos);

		List<ReviewDto> actualReviewDtos = adapter.getReviews(title, authors);

		assertEquals(expectedReviewDtos, actualReviewDtos);
		verify(useCase).getReviews(title, authors);
		verify(mapper).domains2Dtos(domainReviews);
	}
}