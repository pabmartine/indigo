package com.martinia.indigo.serie.application;

import com.martinia.indigo.ports.in.rest.BookService;
import com.martinia.indigo.serie.domain.repository.SerieRepository;
import com.martinia.indigo.serie.domain.service.FindNumSeriesUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class FindNumSeriesUseCaseImpl implements FindNumSeriesUseCase {

	@Resource
	private SerieRepository serieRepository;

	@Override
	public Long getNumSeries(final List<String> languages) {
		return serieRepository.getNumSeries(languages);
	}
}
