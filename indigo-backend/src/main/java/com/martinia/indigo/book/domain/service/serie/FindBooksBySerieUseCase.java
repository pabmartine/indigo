package com.martinia.indigo.book.domain.service.serie;

import com.martinia.indigo.book.domain.model.Book;

import java.util.List;

public interface FindBooksBySerieUseCase {

	List<Book> getSerie(String serie, List<String> languages);

}
