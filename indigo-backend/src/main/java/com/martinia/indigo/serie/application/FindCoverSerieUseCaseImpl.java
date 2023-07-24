package com.martinia.indigo.serie.application;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import com.martinia.indigo.serie.domain.ports.usecases.FindCoverSerieUseCase;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FindCoverSerieUseCaseImpl implements FindCoverSerieUseCase {

	@Resource
	private BookRepository bookRepository;

	@Resource
	private BookMongoMapper bookMongoMapper;

	@Override
	public String getCover(final String serie) {
		List<Book> books = bookMongoMapper.entities2Domains(bookRepository.findBooksBySerie(serie.replace("@_@", "&")));
		List<Book> sorted = books.stream().sorted(Comparator.comparingInt(b -> b.getSerie().getIndex())).collect(Collectors.toList());

		if (!CollectionUtils.isEmpty(sorted)) {
			return sorted.get(0).getImage();
		}

		return null;
	}
}
