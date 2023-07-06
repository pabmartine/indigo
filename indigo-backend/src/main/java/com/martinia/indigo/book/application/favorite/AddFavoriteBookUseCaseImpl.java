package com.martinia.indigo.book.application.favorite;

import com.martinia.indigo.book.domain.service.favorite.AddFavoriteBookUseCase;
import com.martinia.indigo.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Service
@Transactional
public class AddFavoriteBookUseCaseImpl implements AddFavoriteBookUseCase {

	@Resource
	private UserRepository userRepository;

	@Override
	public void addFavoriteBook(String user, String book) {
		userRepository.addFavoriteBook(user, book);

	}

}
