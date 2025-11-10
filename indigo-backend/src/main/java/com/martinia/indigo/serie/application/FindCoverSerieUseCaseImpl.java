package com.martinia.indigo.serie.application;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import com.martinia.indigo.serie.domain.ports.usecases.FindCoverSerieUseCase;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
	public byte[] getCover(final String serie) {
		String decodedSerie = URLDecoder.decode(serie, StandardCharsets.UTF_8);
		List<Book> books = bookMongoMapper.entities2Domains(bookRepository.findBooksBySerie(decodedSerie.replace("@_@", "&")));
		List<Book> sorted = books.stream().sorted(Comparator.comparingInt(b -> b.getSerie().getIndex())).collect(Collectors.toList());

		if (!CollectionUtils.isEmpty(sorted)) {
			String image = sorted.get(0).getImage();
			if (StringUtils.isNotBlank(image)) {
				try {
					return Base64.getDecoder().decode(image);
				}
				catch (IllegalArgumentException ex) {
					return image.getBytes(StandardCharsets.UTF_8);
				}
			}
		}

		return new byte[0];
	}
}
