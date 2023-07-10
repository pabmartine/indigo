package com.martinia.indigo.book.application.language;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.language.FindBookLanguagesUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class FindBookLanguagesUseCaseImpl implements FindBookLanguagesUseCase {

	@Resource
	private BookRepository bookRepository;

	@Override
	public List<String> getBookLanguages() {
		return bookRepository.getBookLanguages();
	}

}
