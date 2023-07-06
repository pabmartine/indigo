package com.martinia.indigo.book.application.favorite;

import com.martinia.indigo.book.domain.service.favorite.CheckIsFavoriteBookUseCase;
import com.martinia.indigo.user.domain.repository.UserRepository;
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
		return userRepository.isFavoriteBook(user, book);

	}

}
