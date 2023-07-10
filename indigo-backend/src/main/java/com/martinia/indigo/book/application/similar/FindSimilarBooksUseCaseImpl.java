package com.martinia.indigo.book.application.similar;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.similar.FindSimilarBooksUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class FindSimilarBooksUseCaseImpl implements FindSimilarBooksUseCase {

	@Resource
	private BookRepository bookRepository;

	@Override
	public List<Book> getSimilar(List<String> similar, List<String> languages) {
		return bookRepository.getSimilar(similar, languages);
	}

}
