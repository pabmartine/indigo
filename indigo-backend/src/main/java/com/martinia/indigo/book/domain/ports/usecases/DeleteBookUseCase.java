package com.martinia.indigo.book.domain.ports.usecases;

import com.martinia.indigo.book.domain.model.Book;

import java.util.Optional;

public interface DeleteBookUseCase {

	void delete(String id);

}
