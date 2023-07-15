package com.martinia.indigo.author.application.favorite;

import com.martinia.indigo.author.domain.ports.usecases.favorite.AddFavoriteAuthorUseCase;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;

@Service
public class AddFavoriteAuthorUseCaseImpl implements AddFavoriteAuthorUseCase {

	@Resource
	private UserRepository userRepository;

	@Override
	public void addFavoriteAuthor(String user, String author) {
		userRepository.findByUsername(user).ifPresent(_user -> {
			if (CollectionUtils.isEmpty(_user.getFavoriteAuthors()) || !_user.getFavoriteAuthors().contains(author)) {
				_user.getFavoriteAuthors().add(author);
				userRepository.save(_user);
			}
		});
	}

}
