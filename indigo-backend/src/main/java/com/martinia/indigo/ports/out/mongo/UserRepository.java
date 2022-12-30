package com.martinia.indigo.ports.out.mongo;

import java.util.List;
import java.util.Optional;

import com.martinia.indigo.domain.model.Author;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.User;

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
