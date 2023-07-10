package com.martinia.indigo.book.domain.ports.usecases.favorite;

public interface CheckIsFavoriteBookUseCase {

	Boolean isFavoriteBook(String book, String user);

}
