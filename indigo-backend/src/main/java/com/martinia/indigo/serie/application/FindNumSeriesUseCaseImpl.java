package com.martinia.indigo.serie.application;

import com.martinia.indigo.serie.domain.ports.repositories.SerieRepository;
import com.martinia.indigo.serie.domain.ports.usecases.FindNumSeriesUseCase;
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
