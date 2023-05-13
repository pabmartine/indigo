package com.martinia.indigo.ports.out.metadata;

import com.martinia.indigo.domain.model.inner.Review;

import java.util.List;

public interface GoodReadsService {

	String[] findAuthor(String key, String subject);

	String[] findBook(String key, String title, List<String> authors, boolean withAuthor);

	List<Review> getReviews(String lang, String title, List<String> authors);

}
