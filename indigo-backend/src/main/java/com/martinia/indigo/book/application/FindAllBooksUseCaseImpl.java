package com.martinia.indigo.book.application;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookMongoRepository;
import com.martinia.indigo.book.domain.ports.usecases.FindAllBooksUseCase;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import com.martinia.indigo.common.model.Search;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class FindAllBooksUseCaseImpl implements FindAllBooksUseCase {

	@Resource
	private BookMongoRepository bookMongoRepository;

	@Resource
	private BookMongoMapper bookMongoMapper;

	@Override
	public List<Book> findAll(Search search, int page, int size, String sort, String order) {
		return bookMongoMapper.entities2Domains(bookMongoRepository.findAll(search, page, size, sort, order));
	}

}
