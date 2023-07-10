package com.martinia.indigo.book.domain.ports.usecases.sent;

import com.martinia.indigo.book.domain.model.Book;

import java.util.List;

public interface FindSentBooksUseCase {

	List<Book> getSentBooks(String user);

}
