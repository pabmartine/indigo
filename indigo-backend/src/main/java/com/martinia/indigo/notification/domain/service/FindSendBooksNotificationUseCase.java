package com.martinia.indigo.notification.domain.service;

import com.martinia.indigo.book.domain.model.Book;

import java.util.List;

public interface FindSendBooksNotificationUseCase {

	List<Book> getSentBooks(String user);

}
