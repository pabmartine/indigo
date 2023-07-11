package com.martinia.indigo.book.application;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookMongoRepository;
import com.martinia.indigo.book.domain.ports.usecases.FindBookByIdUseCase;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class FindBookByIdUseCaseImpl implements FindBookByIdUseCase {

	@Resource
	private BookMongoRepository bookMongoRepository;

	@Resource
	private BookMongoMapper bookMongoMapper;

	@Override
	public Optional<Book> findById(String id) {
		return bookMongoRepository.findById(id).map(book -> Optional.of(bookMongoMapper.entity2Domain(book))).orElse(Optional.empty());
	}

}
