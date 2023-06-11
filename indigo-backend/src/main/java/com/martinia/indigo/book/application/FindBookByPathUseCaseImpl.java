package com.martinia.indigo.book.application;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.repository.BookRepository;
import com.martinia.indigo.book.domain.service.FindBookByPathUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FindBookByPathUseCaseImpl implements FindBookByPathUseCase {

	@Autowired
	BookRepository bookRepository;

	@Override
	public Optional<Book> findByPath(String path) {
		return bookRepository.findByPath(path);
	}

}
