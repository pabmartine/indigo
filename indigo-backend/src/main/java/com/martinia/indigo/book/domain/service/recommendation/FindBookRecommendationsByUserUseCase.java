package com.martinia.indigo.book.domain.service.recommendation;

import com.martinia.indigo.book.domain.model.Book;

import java.util.List;

public interface FindBookRecommendationsByUserUseCase {

	List<Book> getRecommendationsByUser(String user, int page, int size, String sort, String order);

}
