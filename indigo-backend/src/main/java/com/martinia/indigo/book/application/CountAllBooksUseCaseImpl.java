package com.martinia.indigo.book.application;

import com.martinia.indigo.book.domain.ports.repositories.BookMongoRepository;
import com.martinia.indigo.book.domain.ports.usecases.CountAllBooksUseCase;
import com.martinia.indigo.common.model.Search;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Service
@Transactional
public class CountAllBooksUseCaseImpl implements CountAllBooksUseCase {

	@Resource
	private BookMongoRepository bookMongoRepository;

	@Override
	public Long count(Search search) {
		return bookMongoRepository.count(search);
	}

}
