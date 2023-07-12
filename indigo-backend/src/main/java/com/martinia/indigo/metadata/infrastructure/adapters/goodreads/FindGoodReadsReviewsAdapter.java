package com.martinia.indigo.metadata.infrastructure.adapters.goodreads;

import com.martinia.indigo.common.infrastructure.api.mappers.ReviewDtoMapper;
import com.martinia.indigo.common.infrastructure.api.model.ReviewDto;
import com.martinia.indigo.metadata.domain.ports.adapters.goodreads.FindGoodReadsReviewsPort;
import com.martinia.indigo.metadata.domain.ports.usecases.goodreads.FindGoodReadsReviewsUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@ConditionalOnProperty(name = "flags.goodreads", havingValue="true")
public class FindGoodReadsReviewsAdapter implements FindGoodReadsReviewsPort {

	@Resource
	private FindGoodReadsReviewsUseCase useCase;

	@Resource
	private ReviewDtoMapper mapper;

	@Override
	public List<ReviewDto> getReviews(final String lang, final String title, final List<String> authors) {
		return mapper.domains2Dtos(useCase.getReviews(lang, title, authors));
	}
}
