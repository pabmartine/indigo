package com.martinia.indigo.book.application.serie;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.repository.BookRepository;
import com.martinia.indigo.book.domain.service.serie.FindBooksBySerieUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class FindBooksBySerieUseCaseImpl implements FindBooksBySerieUseCase {

	@Resource
	private BookRepository bookRepository;

	@Override
	public List<Book> getSerie(String serie, List<String> languages) {
		return bookRepository.getSerie(serie, languages);
	}

}
