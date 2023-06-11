package com.martinia.indigo.serie.application;

import com.martinia.indigo.serie.domain.repository.SerieRepository;
import com.martinia.indigo.serie.domain.service.FindNumBooksBySerieUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class FindNumBooksBySerieUseCaseImpl implements FindNumBooksBySerieUseCase {

	@Resource
	private SerieRepository serieRepository;

	@Override
	public Map<String, Long> getNumBooksBySerie(final List<String> languages, final int page, final int size, final String sort,
			final String order) {
		return serieRepository.getNumBooksBySerie(languages, page, size, sort, order);
	}
}
