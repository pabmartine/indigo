package com.martinia.indigo.book.application.favorite;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.favorite.FindFavoriteBooksUseCase;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FindFavoriteBooksUseCaseImpl implements FindFavoriteBooksUseCase {

	@Resource
	private UserRepository userRepository;

	@Resource
	private BookRepository bookRepository;

	@Resource
	private BookMongoMapper bookMongoMapper;

	@Override
	public List<Book> getFavoriteBooks(String user) {
		return userRepository.findByUsername(user).map(userEntity -> {
			List<String> books = userEntity.getFavoriteBooks();
			List<Book> ret = new ArrayList<>(books != null ? books.size() : 0);
			if (!CollectionUtils.isEmpty(books)) {
				books.forEach(book -> ret.add(findBook(book)));
			}
			return ret;
		}).orElse(Collections.emptyList());
	}

	private Book findBook(String path) {
		return bookRepository.findByPath(path).map(book -> bookMongoMapper.entity2Domain(book)).orElse(null);
	}

}
