package com.martinia.indigo.adapters.out.mongo.service;

import com.martinia.indigo.adapters.out.mongo.entities.BookMongoEntity;
import com.martinia.indigo.adapters.out.mongo.mapper.BookMongoMapper;
import com.martinia.indigo.adapters.out.mongo.repository.BookMongoRepository;
import com.martinia.indigo.adapters.out.mongo.repository.ViewMongoRepository;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.Search;
import com.martinia.indigo.ports.out.mongo.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BookRepositoryImpl implements BookRepository {

	@Autowired
	BookMongoRepository bookMongoRepository;

	@Autowired
	ViewMongoRepository viewMongoRepository;

	@Autowired
	BookMongoMapper bookMongoMapper;

	@Override
	public Long count(Search search) {
		return bookMongoRepository.count(search);
	}

	@Override
	public List<Book> findAll(Search search, int page, int size, String sort, String order) {
		return bookMongoMapper.entities2Domains(bookMongoRepository.findAll(search, page, size, sort, order));
	}

	@Override
	public Optional<Book> findById(String id) {
		return bookMongoRepository.findById(id).map(book -> Optional.of(bookMongoMapper.entity2Domain(book))).orElse(Optional.empty());
	}

	public Optional<Book> findByTitle(String title) {
		return bookMongoRepository.findByTitle(title).map(book -> Optional.of(bookMongoMapper.entity2Domain(book)))
				.orElse(Optional.empty());

	}

	@Override
	public List<Book> getRecommendationsByBook(Book book) {
		return bookMongoMapper.entities2Domains(bookMongoRepository.getRecommendationsByBook(bookMongoMapper.domain2Entity(book)));
	}

	@Override
	public List<Book> getRecommendationsByBook(List<String> recommendations, List<String> languages, int num) {
		return bookMongoMapper.entities2Domains(bookMongoRepository.getRecommendationsByBook(recommendations, languages, num));
	}

	@Override
	public Long countRecommendationsByUser(String user) {
		return bookMongoRepository.countRecommendationsByUser(user);
	}

	@Override
	public List<Book> getRecommendationsByUser(String user, int page, int size, String sort, String order) {
		List<Book> ret = null;

		List<Book> books = bookMongoMapper.entities2Domains(bookMongoRepository.getRecommendationsByUser(user, page, size, sort, order));

		Map<String, Book> map = books.stream().collect(LinkedHashMap::new, (_map, item) -> _map.put(item.getId(), item), Map::putAll);

		if (map != null && !map.isEmpty()) {
			ret = map.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
		}
		return ret;

	}

	@Override
	public List<Book> getSimilar(List<String> similar, List<String> languages) {
		return bookMongoMapper.entities2Domains(bookMongoRepository.getSimilar(similar, languages));
	}

	@Override
	public List<Book> getSerie(String serie, List<String> languages) {
		return bookMongoMapper.entities2Domains(bookMongoRepository.getSerie(serie, languages));
	}

	@Override
	public void save(Book book) {
		bookMongoRepository.save(bookMongoMapper.domain2Entity(book));
	}

	@Override
	public void dropCollection() {
		bookMongoRepository.dropCollection(BookMongoEntity.class);
	}

	public Optional<Book> findByPath(String path) {
		return bookMongoRepository.findByPath(path).map(book -> Optional.of(bookMongoMapper.entity2Domain(book))).orElse(Optional.empty());
	}

	@Override
	public List<String> getBookLanguages() {
		return bookMongoRepository.getBookLanguages();
	}

}
