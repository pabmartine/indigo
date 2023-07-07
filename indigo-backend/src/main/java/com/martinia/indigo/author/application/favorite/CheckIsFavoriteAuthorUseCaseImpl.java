package com.martinia.indigo.author.application.favorite;

import com.martinia.indigo.author.domain.service.favorite.CheckIsFavoriteAuthorUseCase;
import com.martinia.indigo.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CheckIsFavoriteAuthorUseCaseImpl implements CheckIsFavoriteAuthorUseCase {

	@Resource
	private UserRepository userRepository;

	@Override
	public Boolean isFavoriteAuthor(String user, String author) {
		return userRepository.isFavoriteAuthor(user, author);

	}

}
