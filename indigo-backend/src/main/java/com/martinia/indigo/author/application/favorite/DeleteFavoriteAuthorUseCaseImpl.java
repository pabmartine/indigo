package com.martinia.indigo.author.application.favorite;

import com.martinia.indigo.author.domain.ports.usecases.favorite.DeleteFavoriteAuthorUseCase;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DeleteFavoriteAuthorUseCaseImpl implements DeleteFavoriteAuthorUseCase {

	@Resource
	private UserRepository userRepository;

	@Override
	public void deleteFavoriteAuthor(String user, String author) {
		userRepository.deleteFavoriteAuthor(user, author);
	}

}