package com.martinia.indigo.metadata.domain.ports.usecases.goodreads;

import com.martinia.indigo.common.model.Review;

import java.util.List;

public interface FindGoodReadsReviewsUseCase {

	List<Review> getReviews(String lang, String title, List<String> authors);

}
