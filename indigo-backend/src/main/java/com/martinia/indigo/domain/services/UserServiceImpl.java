package com.martinia.indigo.domain.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.martinia.indigo.domain.enums.RolesEnum;
import com.martinia.indigo.domain.model.Author;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.User;
import com.martinia.indigo.ports.in.rest.UserService;
import com.martinia.indigo.ports.out.mongo.UserRepository;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Override
	public Optional<User> findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	@Override
	public Optional<User> findById(String id) {
		return userRepository.findById(id);
	}

	@Override
	public void save(User user, boolean isNew) {
		if (isNew) {
			user.setRole(RolesEnum.USER.name());
			user.setPassword(passwordEncoder.encode(user.getPassword()));
		}
		userRepository.save(user);
	}

	@Override
	public List<User> findAll() {
		return userRepository.findAll();
	}

	@Override
	public void delete(String id) {
		userRepository.delete(id);
	}

	@Override
	public void update(User user) {
		User _user = this.findById(user.getId()).get();

		if (!_user.getPassword()
				.equals(user.getPassword()))
			_user.setPassword(passwordEncoder.encode(user.getPassword()));

		_user.setUsername(user.getUsername());
		_user.setLanguage(user.getLanguage());
		_user.setLanguageBooks(user.getLanguageBooks());
		_user.setKindle(user.getKindle());

		this.save(_user, false);
	}

	@Override
	public void addFavoriteBook(String user, String book) {
		userRepository.addFavoriteBook(user, book);

	}

	@Override
	public void addFavoriteAuthor(String user, String author) {
		userRepository.addFavoriteAuthor(user, author);

	}

	@Override
	public void deleteFavoriteBook(String user, String book) {
		userRepository.deleteFavoriteBook(user, book);
	}

	@Override
	public Boolean isFavoriteBook(String user, String book) {
		return userRepository.isFavoriteBook(user, book);

	}
	
	@Override
	public List<Author> getFavoriteAuthors(String user) {
		return userRepository.getFavoriteAuthors(user);

	}

	@Override
	public List<Book> getFavoriteBooks(String user) {
		return userRepository.getFavoriteBooks(user);

	}

	@Override
	public Boolean isFavoriteAuthor(String user, String author) {
		return userRepository.isFavoriteAuthor(user, author);

	}

	@Override
	public void deleteFavoriteAuthor(String user, String author) {
		userRepository.deleteFavoriteAuthor(user, author);
	}

}
