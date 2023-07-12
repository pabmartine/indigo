package com.martinia.indigo.book.application.favorite;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.favorite.FindFavoriteBooksUseCase;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
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
		List<String> books = userRepository.findByUsername(user).get().getFavoriteBooks();
		List<Book> ret = new ArrayList<>(books != null ? books.size() : 0);
		if (books != null) {
			books.forEach(path -> {
				Optional<Book> _book = bookRepository.findByPath(path).map(book -> Optional.of(bookMongoMapper.entity2Domain(book)))
						.orElse(Optional.empty());
				if (_book.isPresent()) {
					ret.add(_book.get());
				}
			});
		}
		return ret;

	}

}
