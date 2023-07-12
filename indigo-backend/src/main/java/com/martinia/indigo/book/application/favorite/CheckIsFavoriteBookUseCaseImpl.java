package com.martinia.indigo.book.application.favorite;

import com.martinia.indigo.book.domain.ports.usecases.favorite.CheckIsFavoriteBookUseCase;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Service
@Transactional
public class CheckIsFavoriteBookUseCaseImpl implements CheckIsFavoriteBookUseCase {

	@Resource
	private UserRepository userRepository;

	@Override
	public Boolean isFavoriteBook(String user, String book) {
		UserMongoEntity entity = userRepository.findByUsername(user).get();
		return entity.getFavoriteBooks().stream().filter(favorite -> favorite.equalsIgnoreCase(book)).findAny().isPresent();
	}

}
