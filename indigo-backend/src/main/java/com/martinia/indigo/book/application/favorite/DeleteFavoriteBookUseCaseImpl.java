package com.martinia.indigo.book.application.favorite;

import com.martinia.indigo.book.domain.ports.usecases.favorite.DeleteFavoriteBookUseCase;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class DeleteFavoriteBookUseCaseImpl implements DeleteFavoriteBookUseCase {

	@Autowired
	private UserRepository userRepository;

	@Override
	public void deleteFavoriteBook(String user, String book) {
		UserMongoEntity _user = userRepository.findByUsername(user).get();
		_user.getFavoriteBooks().remove(book);
		userRepository.save(_user);
	}

}
