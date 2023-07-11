package com.martinia.indigo.book.application.serie;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookMongoRepository;
import com.martinia.indigo.book.domain.ports.usecases.serie.FindBooksBySerieUseCase;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class FindBooksBySerieUseCaseImpl implements FindBooksBySerieUseCase {

	@Resource
	private BookMongoRepository bookMongoRepository;

	@Resource
	private BookMongoMapper bookMongoMapper;

	@Override
	public List<Book> getSerie(String serie, List<String> languages) {
		return bookMongoMapper.entities2Domains(bookMongoRepository.getSerie(serie, languages));
	}

}
