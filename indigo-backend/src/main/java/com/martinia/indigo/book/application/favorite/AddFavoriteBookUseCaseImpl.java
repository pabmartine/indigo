package com.martinia.indigo.book.application.favorite;

import com.martinia.indigo.book.domain.ports.usecases.favorite.AddFavoriteBookUseCase;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Service
@Transactional
public class AddFavoriteBookUseCaseImpl implements AddFavoriteBookUseCase {

	@Resource
	private UserRepository userRepository;

	@Override
	public void addFavoriteBook(String user, String book) {
		UserMongoEntity _user = userRepository.findByUsername(user).get();
		if (CollectionUtils.isEmpty(_user.getFavoriteBooks()) || !_user.getFavoriteBooks().contains(book)) {
			_user.getFavoriteBooks().add(book);
			userRepository.save(_user);
		}

	}

}
