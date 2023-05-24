package com.martinia.indigo.serie.application;

import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.ports.in.rest.BookService;
import com.martinia.indigo.serie.domain.repository.SerieRepository;
import com.martinia.indigo.serie.domain.service.FindCoverSerieUseCase;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FindCoverSerieUseCaseImpl implements FindCoverSerieUseCase {

	@Resource
	private SerieRepository serieRepository;

	@Override
	public String getCover(final String serie) {

		List<Book> books = serieRepository.findBooksBySerie(serie.replace("@_@", "&"));
		List<Book> sorted = books.stream().sorted(Comparator.comparingInt(b -> b.getSerie().getIndex())).collect(Collectors.toList());

		if (!CollectionUtils.isEmpty(sorted)) {
			return sorted.get(0).getImage();
		}

		return null;
	}
}
