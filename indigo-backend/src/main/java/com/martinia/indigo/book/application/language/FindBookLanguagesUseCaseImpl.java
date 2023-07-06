package com.martinia.indigo.book.application.language;

import com.martinia.indigo.book.domain.repository.BookRepository;
import com.martinia.indigo.book.domain.service.language.FindBookLanguagesUseCase;
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
