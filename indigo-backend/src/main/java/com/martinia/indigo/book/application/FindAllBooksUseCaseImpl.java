package com.martinia.indigo.book.application;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.repository.BookRepository;
import com.martinia.indigo.book.domain.service.FindAllBooksUseCase;
import com.martinia.indigo.domain.model.Search;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class FindAllBooksUseCaseImpl implements FindAllBooksUseCase {

	@Resource
	private BookRepository bookRepository;

	@Override
	public List<Book> findAll(Search search, int page, int size, String sort, String order) {
		return bookRepository.findAll(search, page, size, sort, order);
	}

}
