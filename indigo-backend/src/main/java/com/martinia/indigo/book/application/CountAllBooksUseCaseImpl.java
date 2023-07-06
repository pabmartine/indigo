package com.martinia.indigo.book.application;

import com.martinia.indigo.book.domain.repository.BookRepository;
import com.martinia.indigo.book.domain.service.CountAllBooksUseCase;
import com.martinia.indigo.domain.model.Search;
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
