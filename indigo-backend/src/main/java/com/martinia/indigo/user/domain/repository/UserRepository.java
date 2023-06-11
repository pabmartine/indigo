package com.martinia.indigo.user.domain.repository;

import com.martinia.indigo.domain.model.Author;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.user.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

	void addFavoriteAuthor(String user, String author);

	void addFavoriteBook(String user, String book);

	void delete(String id);

	void deleteFavoriteAuthor(String user, String author);

	void deleteFavoriteBook(String user, String book);

	List<User> findAll();

	Optional<User> findById(String id);

	Optional<User> findByUsername(String string);

	List<Book> getFavoriteBooks(String user);
	
	List<Author> getFavoriteAuthors(String user);

	Boolean isFavoriteAuthor(String user, String author);

	Boolean isFavoriteBook(String user, String book);

	void save(User user);


}
