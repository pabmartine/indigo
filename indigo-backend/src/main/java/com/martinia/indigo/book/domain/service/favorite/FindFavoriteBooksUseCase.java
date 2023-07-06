package com.martinia.indigo.book.domain.service.favorite;

import com.martinia.indigo.book.domain.model.Book;

import java.util.List;

public interface FindFavoriteBooksUseCase {

	List<Book> getFavoriteBooks(String user);

}
