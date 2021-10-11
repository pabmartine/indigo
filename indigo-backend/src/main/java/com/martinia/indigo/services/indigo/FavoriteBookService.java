package com.martinia.indigo.services.indigo;

import com.martinia.indigo.model.indigo.FavoriteBook;

public interface FavoriteBookService {

	FavoriteBook getFavoriteBook(int book, int user);

	void delete(FavoriteBook fb);

	void save(FavoriteBook fb);

}
