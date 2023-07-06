package com.martinia.indigo.book.application.favorite;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.service.favorite.FindFavoriteBooksUseCase;
import com.martinia.indigo.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class FindFavoriteBooksUseCaseImpl implements FindFavoriteBooksUseCase {

	@Resource
	private UserRepository userRepository;

	@Override
	public List<Book> getFavoriteBooks(String user) {
		return userRepository.getFavoriteBooks(user);

	}

}
