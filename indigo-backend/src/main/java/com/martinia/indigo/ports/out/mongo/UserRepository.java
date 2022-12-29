package com.martinia.indigo.ports.out.mongo;

import java.util.List;
import java.util.Optional;

import com.martinia.indigo.domain.model.Author;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.User;

public interface UserRepository {

	public void addFavoriteAuthor(String user, String author);

	public void addFavoriteBook(String user, String book);

	public void delete(String id);

	public void deleteFavoriteAuthor(String user, String author);

	public void deleteFavoriteBook(String user, String book);

	public List<User> findAll();

	public Optional<User> findById(String id);

	public Optional<User> findByUsername(String string);

	public List<Book> getFavoriteBooks(String user);
	
	public List<Author> getFavoriteAuthors(String user);

	public Boolean isFavoriteAuthor(String user, String author);

	public Boolean isFavoriteBook(String user, String book);

	public void save(User user);


}
