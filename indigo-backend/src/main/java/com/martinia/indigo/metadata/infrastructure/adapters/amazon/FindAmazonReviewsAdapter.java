package com.martinia.indigo.metadata.infrastructure.adapters.amazon;

import com.martinia.indigo.common.infrastructure.mapper.ReviewDtoMapper;
import com.martinia.indigo.common.infrastructure.model.ReviewDto;
import com.martinia.indigo.metadata.domain.ports.adapters.amazon.FindAmazonReviewsPort;
import com.martinia.indigo.metadata.domain.ports.usecases.amazon.FindAmazonReviewsUseCase;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class FindAmazonReviewsAdapter implements FindAmazonReviewsPort {

	@Resource
	private FindAmazonReviewsUseCase useCase;

	@Resource
	private ReviewDtoMapper mapper;

	@Override
	public List<ReviewDto> getReviews(final String title, final List<String> authors) {
		return mapper.domains2Dtos(useCase.getReviews(title, authors));
	}
}
