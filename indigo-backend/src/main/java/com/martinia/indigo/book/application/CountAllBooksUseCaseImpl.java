package com.martinia.indigo.book.application;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.CountAllBooksUseCase;
import com.martinia.indigo.common.domain.model.Search;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Service
@Transactional
public class CountAllBooksUseCaseImpl implements CountAllBooksUseCase {

	@Resource
	private BookRepository bookRepository;

	@Override
	public Long count(Search search) {
		return bookRepository.count(search);
	}

}
