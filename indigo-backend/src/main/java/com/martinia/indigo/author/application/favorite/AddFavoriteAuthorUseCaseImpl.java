package com.martinia.indigo.author.application.favorite;

import com.martinia.indigo.author.domain.ports.usecases.favorite.AddFavoriteAuthorUseCase;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;

@Service
public class AddFavoriteAuthorUseCaseImpl implements AddFavoriteAuthorUseCase {

	@Resource
	private UserRepository userRepository;

	@Override
	public void addFavoriteAuthor(String user, String author) {
		UserMongoEntity _user = userRepository.findByUsername(user).get();
		if (CollectionUtils.isEmpty(_user.getFavoriteAuthors()) || !_user.getFavoriteAuthors().contains(author)) {
			_user.getFavoriteAuthors().add(author);
			userRepository.save(_user);
		}

	}

}
