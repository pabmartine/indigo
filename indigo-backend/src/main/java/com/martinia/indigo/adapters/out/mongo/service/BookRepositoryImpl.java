package com.martinia.indigo.adapters.out.mongo.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.martinia.indigo.adapters.out.mongo.entities.BookMongoEntity;
import com.martinia.indigo.adapters.out.mongo.mapper.BookMongoMapper;
import com.martinia.indigo.adapters.out.mongo.repository.BookMongoRepository;
import com.martinia.indigo.adapters.out.mongo.repository.NotificationMongoRepository;
import com.martinia.indigo.adapters.out.mongo.repository.ViewMongoRepository;
import com.martinia.indigo.domain.model.Book;
import com.martinia.indigo.domain.model.Search;
import com.martinia.indigo.ports.out.mongo.BookRepository;

@Component
public class BookRepositoryImpl implements BookRepository {

	@Autowired
	BookMongoRepository bookMongoRepository;

	@Autowired
	NotificationMongoRepository notificationMongoRepository;

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
	public List<Book> findBooksBySerie(String serie) {
		return bookMongoMapper.entities2Domains(bookMongoRepository.findBooksBySerie(serie));
	}

	@Override
	public Book findById(String id) {
		return bookMongoMapper.entity2Domain(bookMongoRepository.findById(id)
				.orElse(null));
	}

	public Book findByTitle(String title) {
		return bookMongoMapper.entity2Domain(bookMongoRepository.findByTitle(title));

	}

	@Override
	public List<Book> getRecommendationsByBook(String id) {
		return bookMongoMapper.entities2Domains(bookMongoRepository.getRecommendationsByBook(id));
	}

	@Override
	public List<Book> getRecommendationsByBook(List<String> recommendations, int num) {
		return bookMongoMapper.entities2Domains(bookMongoRepository.getRecommendationsByBook(recommendations, num));
	}

	@Override
	public List<Book> getRecommendationsByUser(String user, int num) {
		List<Book> ret = null;

		List<Book> books = bookMongoMapper.entities2Domains(notificationMongoRepository.getRecommendations(user, num));
		List<Book> books2 = bookMongoMapper.entities2Domains(viewMongoRepository.getRecommendations(user, num));

		Map<String, Book> map1 = books.stream()
				.collect(
                        LinkedHashMap::new,                           
                        (map, item) -> map.put(item.getId(), item), 
                        Map::putAll);

		Map<String, Book> map2 = books2.stream()
				.collect(LinkedHashMap::new,                           
                        (map, item) -> map.put(item.getId(), item), 
                        Map::putAll);

		Map<String, Book> map3 = new LinkedHashMap<>();

		map3.putAll(map1);
		map3.putAll(map2);

		if (map3 != null && !map3.isEmpty()) {
			ret = map3.entrySet()
					.stream()
					.map(Map.Entry::getValue)
					.collect(Collectors.toList())
					.subList(0, num > map3.entrySet()
							.size() ? map3.entrySet()
									.size() : num);
		}
		return ret;

	}

	@Override
	public Map<String, Long> getNumBooksBySerie(int page, int size, String sort, String order) {
		return bookMongoRepository.getNumBooksBySerie(page, size, sort, order);
	}

	@Override
	public Long getNumSeries() {
		return bookMongoRepository.getNumSeries();
	}

	@Override
	public List<Book> getSimilar(List<String> similar) {
		return bookMongoMapper.entities2Domains(bookMongoRepository.getSimilar(similar));
	}

	@Override
	public void save(Book book) {
		bookMongoRepository.save(bookMongoMapper.domain2Entity(book));
	}

	@Override
	public void dropCollection() {
		bookMongoRepository.dropCollection(BookMongoEntity.class);
	}

	public Book findByPath(String path) {
		return bookMongoMapper.entity2Domain(bookMongoRepository.findByPath(path));

	}

}
