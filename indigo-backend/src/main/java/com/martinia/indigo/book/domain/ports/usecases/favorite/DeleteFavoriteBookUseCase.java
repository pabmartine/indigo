package com.martinia.indigo.book.domain.ports.usecases.favorite;

public interface DeleteFavoriteBookUseCase {

	void deleteFavoriteBook(String book, String user);

}
