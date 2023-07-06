package com.martinia.indigo.domain.services;

import com.martinia.indigo.domain.model.Author;
import com.martinia.indigo.ports.in.rest.UserService;
import com.martinia.indigo.user.domain.repository.UserRepository;
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
	public void addFavoriteAuthor(String user, String author) {
		userRepository.addFavoriteAuthor(user, author);

	}

	@Override
	public List<Author> getFavoriteAuthors(String user) {
		return userRepository.getFavoriteAuthors(user);

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
