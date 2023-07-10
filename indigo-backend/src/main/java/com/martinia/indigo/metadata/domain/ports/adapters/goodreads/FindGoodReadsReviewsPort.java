package com.martinia.indigo.metadata.domain.ports.adapters.goodreads;

import com.martinia.indigo.common.infrastructure.model.ReviewDto;
import com.martinia.indigo.common.model.Review;

import java.util.List;

public interface FindGoodReadsReviewsPort {

	List<ReviewDto> getReviews(String lang, String title, List<String> authors);

}
