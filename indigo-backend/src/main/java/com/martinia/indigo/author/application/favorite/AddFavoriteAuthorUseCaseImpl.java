package com.martinia.indigo.author.application.favorite;

import com.martinia.indigo.author.domain.ports.usecases.favorite.AddFavoriteAuthorUseCase;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AddFavoriteAuthorUseCaseImpl implements AddFavoriteAuthorUseCase {

	@Resource
	private UserRepository userRepository;

	@Override
	public void addFavoriteAuthor(String user, String author) {
		userRepository.addFavoriteAuthor(user, author);

	}

}
