package com.martinia.indigo.serie.application;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.serie.domain.model.SeriePageData;
import com.martinia.indigo.serie.domain.ports.usecases.FindNumBooksBySerieUseCase;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FindNumBooksBySerieUseCaseImpl implements FindNumBooksBySerieUseCase {

	@Resource
	private BookRepository bookRepository;

	@Override
	public Map<String, Long> getNumBooksBySerie(final List<String> languages, final int page, final int size, final String sort,
			final String order) {
		return bookRepository.getNumBooksBySerie(languages, page, size, sort, order);
	}

	@Override
	public SeriePageData getSeriesPage(final List<String> languages, final int page, final int size, final String sort,
			final String order) {
		return bookRepository.getSeriesPage(languages, page, size, sort, order);
	}
}
