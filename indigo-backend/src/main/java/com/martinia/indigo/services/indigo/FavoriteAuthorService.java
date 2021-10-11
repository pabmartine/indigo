package com.martinia.indigo.services.indigo;

import com.martinia.indigo.model.indigo.FavoriteAuthor;

public interface FavoriteAuthorService {

	void delete(FavoriteAuthor fb);

	FavoriteAuthor getFavoriteAuthor(int author, int user);

	void save(FavoriteAuthor fb);

}
