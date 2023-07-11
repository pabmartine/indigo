package com.martinia.indigo.book.application.recommendation;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookMongoRepository;
import com.martinia.indigo.book.domain.ports.usecases.recommendation.FindBookRecommendationsByUserUseCase;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class FindBookRecommendationsByUserUseCaseImpl implements FindBookRecommendationsByUserUseCase {

	@Resource
	private BookMongoRepository bookMongoRepository;

	@Resource
	private BookMongoMapper bookMongoMapper;

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

}
