package com.martinia.indigo.book.domain.service.similar;

import com.martinia.indigo.book.domain.model.Book;

import java.util.List;

public interface FindSimilarBooksUseCase {

	List<Book> getSimilar(List<String> similar, List<String> languages);

}
