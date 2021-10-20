package com.martinia.indigo.ports.in.rest;

import java.util.List;

import com.martinia.indigo.domain.model.Author;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.User;

public interface UserService {

	void addFavoriteAuthor(String user, String author);

	void addFavoriteBook(String user, String book);

	void delete(String id);

	void deleteFavoriteAuthor(String user, String valueOf);

	void deleteFavoriteBook(String book, String user);

	List<User> findAll();

	User findById(String id);

	User findByUsername(String username);

	List<Author> getFavoriteAuthors(String user);
	
	List<Book> getFavoriteBooks(String user);

	Boolean isFavoriteAuthor(String user, String valueOf);

	Boolean isFavoriteBook(String book, String user);

	void save(User user, boolean isNew);

	void update(User user);




}
