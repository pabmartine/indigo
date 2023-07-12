package com.martinia.indigo.author.application.favorite;

import com.martinia.indigo.author.domain.ports.usecases.favorite.CheckIsFavoriteAuthorUseCase;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CheckIsFavoriteAuthorUseCaseImpl implements CheckIsFavoriteAuthorUseCase {

	@Resource
	private UserRepository userRepository;

	@Override
	public Boolean isFavoriteAuthor(String user, String author) {
		UserMongoEntity _user = userRepository.findByUsername(user).get();
		return _user.getFavoriteAuthors().stream().filter(favorite -> favorite.equalsIgnoreCase(author)).findAny().isPresent();

	}

}
