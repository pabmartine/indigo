package com.martinia.indigo.book.domain.ports.usecases.recommendation;

import com.martinia.indigo.book.domain.model.Book;

import java.util.List;

public interface FindBookRecommendationsByBookUseCase {

	List<Book> getRecommendationsByBook(List<String> recommendations, List<String> languages);

}
