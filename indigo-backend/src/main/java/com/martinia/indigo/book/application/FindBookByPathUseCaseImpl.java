package com.martinia.indigo.book.application;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookMongoRepository;
import com.martinia.indigo.book.domain.ports.usecases.FindBookByPathUseCase;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class FindBookByPathUseCaseImpl implements FindBookByPathUseCase {

	@Resource
	private BookMongoRepository bookMongoRepository;

	@Resource
	private BookMongoMapper bookMongoMapper;

	@Override
	public Optional<Book> findByPath(String path) {
		return bookMongoRepository.findByPath(path).map(book -> Optional.of(bookMongoMapper.entity2Domain(book))).orElse(Optional.empty());
	}

}
