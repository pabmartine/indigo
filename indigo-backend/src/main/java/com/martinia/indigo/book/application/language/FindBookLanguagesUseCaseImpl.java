package com.martinia.indigo.book.application.language;

import com.martinia.indigo.book.domain.ports.repositories.BookMongoRepository;
import com.martinia.indigo.book.domain.ports.usecases.language.FindBookLanguagesUseCase;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class FindBookLanguagesUseCaseImpl implements FindBookLanguagesUseCase {

	@Resource
	private BookMongoRepository bookMongoRepository;

	@Resource
	private BookMongoMapper bookMongoMapper;

	@Override
	public List<String> getBookLanguages() {
		return bookMongoRepository.getBookLanguages();
	}

}
