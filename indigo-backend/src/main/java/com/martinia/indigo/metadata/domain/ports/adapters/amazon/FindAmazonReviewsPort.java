package com.martinia.indigo.metadata.domain.ports.adapters.amazon;

import com.martinia.indigo.common.infrastructure.model.ReviewDto;
import com.martinia.indigo.common.model.Review;

import java.util.List;

public interface FindAmazonReviewsPort {

	List<ReviewDto> getReviews(String title, List<String> authors);

}
