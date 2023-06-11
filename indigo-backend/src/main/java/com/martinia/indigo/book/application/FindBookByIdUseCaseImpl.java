package com.martinia.indigo.book.application;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.repository.BookRepository;
import com.martinia.indigo.book.domain.service.FindBookByIdUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Service
public class FindBookByIdUseCaseImpl implements FindBookByIdUseCase {

	@Resource
	BookRepository bookRepository;

	@Override
	public Optional<Book> findById(String id) {
		return bookRepository.findById(id);
	}

}
