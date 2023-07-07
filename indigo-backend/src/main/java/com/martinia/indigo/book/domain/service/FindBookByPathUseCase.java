package com.martinia.indigo.book.domain.service;

import com.martinia.indigo.book.domain.model.Book;

import java.util.Optional;

public interface FindBookByPathUseCase {

	Optional<Book> findByPath(String path);

}
