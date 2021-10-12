package com.martinia.indigo.services.indigo;

import com.martinia.indigo.model.indigo.FavoriteAuthor;

public interface FavoriteAuthorService {

	FavoriteAuthor getFavoriteAuthor(int author, int user);

	void save(FavoriteAuthor fb);

	void deleteFavoriteAuthors(int author, int user);

}
