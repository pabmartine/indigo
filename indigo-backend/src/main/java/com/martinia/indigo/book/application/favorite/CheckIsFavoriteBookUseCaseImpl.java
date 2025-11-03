package com.martinia.indigo.book.application.favorite;

import com.martinia.indigo.book.domain.ports.usecases.favorite.CheckIsFavoriteBookUseCase;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CheckIsFavoriteBookUseCaseImpl implements CheckIsFavoriteBookUseCase {

	@Resource
	private UserRepository userRepository;

	@Override
	public Boolean isFavoriteBook(String user, String book) {
		return userRepository.findByUsername(user)
				.map(_user -> _user.getFavoriteBooks().stream().filter(favorite -> favorite.equalsIgnoreCase(book)).findAny().isPresent())
				.orElse(Boolean.FALSE);
	}

}
