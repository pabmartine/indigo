package com.martinia.indigo.ports.out.metadata;

import com.martinia.indigo.common.model.Review;

import java.util.List;

public interface AmazonService {

	List<Review> getReviews(String title, List<String> authors);

}
