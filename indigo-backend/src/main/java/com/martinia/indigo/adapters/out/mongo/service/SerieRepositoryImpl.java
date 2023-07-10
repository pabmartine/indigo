package com.martinia.indigo.adapters.out.mongo.service;

import com.martinia.indigo.adapters.out.mongo.mapper.BookMongoMapper;
import com.martinia.indigo.adapters.out.mongo.repository.BookMongoRepository;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.serie.domain.ports.repositories.SerieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SerieRepositoryImpl implements SerieRepository {

	@Autowired
	BookMongoRepository bookMongoRepository;

	@Autowired
	BookMongoMapper bookMongoMapper;

	@Override
	public List<Book> findBooksBySerie(String serie) {
		return bookMongoMapper.entities2Domains(bookMongoRepository.findBooksBySerie(serie));
	}

	@Override
	public Map<String, Long> getNumBooksBySerie(List<String> languages, int page, int size, String sort, String order) {
		return bookMongoRepository.getNumBooksBySerie(languages, page, size, sort, order);
	}

	@Override
	public Long getNumSeries(List<String> languages) {
		return bookMongoRepository.getNumSeries(languages);
	}

}
