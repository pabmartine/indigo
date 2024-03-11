package com.martinia.indigo.serie.application;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.serie.domain.ports.usecases.FindNumSeriesUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class FindNumSeriesUseCaseImpl implements FindNumSeriesUseCase {

	@Resource
	private BookRepository bookRepository;

	@Override
	public Long getNumSeries(final List<String> languages) {
		return bookRepository.getNumSeries(languages);

	}
}
