package com.martinia.indigo.book.application.similar;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookMongoRepository;
import com.martinia.indigo.book.domain.ports.usecases.similar.FindSimilarBooksUseCase;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class FindSimilarBooksUseCaseImpl implements FindSimilarBooksUseCase {

	@Resource
	private BookMongoRepository bookMongoRepository;

	@Resource
	private BookMongoMapper bookMongoMapper;

	@Override
	public List<Book> getSimilar(List<String> similar, List<String> languages) {
		return bookMongoMapper.entities2Domains(bookMongoRepository.getSimilar(similar, languages));
	}

}
