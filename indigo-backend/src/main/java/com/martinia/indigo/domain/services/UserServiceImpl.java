package com.martinia.indigo.domain.services;

import com.martinia.indigo.domain.enums.RolesEnum;
import com.martinia.indigo.domain.model.Author;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.User;
import com.martinia.indigo.ports.in.rest.UserService;
import com.martinia.indigo.ports.out.mongo.UserRepository;
import com.martinia.indigo.user.domain.service.FindUserByIdUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	FindUserByIdUseCase findUserByIdUseCase;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Override
	public void save(User user, boolean isNew) {
		if (isNew) {
			user.setRole(RolesEnum.USER.name());
			user.setPassword(passwordEncoder.encode(user.getPassword()));
		}
		userRepository.save(user);
	}

	@Override
	public void delete(String id) {
		userRepository.delete(id);
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
