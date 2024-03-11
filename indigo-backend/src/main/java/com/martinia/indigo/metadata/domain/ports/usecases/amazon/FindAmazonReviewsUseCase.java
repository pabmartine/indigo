package com.martinia.indigo.metadata.domain.ports.usecases.amazon;

import com.martinia.indigo.common.domain.model.Review;

import java.util.List;

public interface FindAmazonReviewsUseCase {

	List<Review> getReviews(String title, List<String> authors);

}
