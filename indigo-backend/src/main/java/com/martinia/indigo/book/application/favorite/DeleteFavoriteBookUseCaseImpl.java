package com.martinia.indigo.book.application.favorite;

import com.martinia.indigo.book.domain.service.favorite.DeleteFavoriteBookUseCase;
import com.martinia.indigo.user.domain.repository.UserRepository;
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
		userRepository.deleteFavoriteBook(user, book);
	}

}
