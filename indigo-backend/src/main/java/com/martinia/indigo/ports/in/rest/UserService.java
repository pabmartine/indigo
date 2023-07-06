package com.martinia.indigo.ports.in.rest;

import com.martinia.indigo.domain.model.Author;

import java.util.List;

public interface UserService {

	void addFavoriteAuthor(String user, String author);

	void deleteFavoriteAuthor(String user, String valueOf);

	List<Author> getFavoriteAuthors(String user);

	Boolean isFavoriteAuthor(String user, String valueOf);

}
