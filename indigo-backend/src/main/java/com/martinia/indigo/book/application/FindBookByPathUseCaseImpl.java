package com.martinia.indigo.book.application;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.FindBookByPathUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class FindBookByPathUseCaseImpl implements FindBookByPathUseCase {

	@Resource
	private BookRepository bookRepository;

	@Override
	public Optional<Book> findByPath(String path) {
		return bookRepository.findByPath(path);
	}

}
