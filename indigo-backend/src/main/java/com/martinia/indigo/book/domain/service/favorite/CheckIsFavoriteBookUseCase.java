package com.martinia.indigo.book.domain.service.favorite;

public interface CheckIsFavoriteBookUseCase {

	Boolean isFavoriteBook(String book, String user);

}
