package com.martinia.indigo.book.application.recommendation;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookMongoRepository;
import com.martinia.indigo.book.domain.ports.usecases.recommendation.FindBookRecommendationsByBookUseCase;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationMongoRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class FindBookRecommendationsByBookUseCaseImpl implements FindBookRecommendationsByBookUseCase {

	@Resource
	private BookMongoRepository bookMongoRepository;

	@Resource
	private ConfigurationMongoRepository configurationMongoRepository;

	@Resource
	private BookMongoMapper bookMongoMapper;

	@Override
	public List<Book> getRecommendationsByBook(List<String> recommendations, List<String> languages) {
		int num = configurationMongoRepository.findByKey("books.recommendations").map(rec -> Integer.parseInt(rec.getValue())).orElse(0);
		return bookMongoMapper.entities2Domains(bookMongoRepository.getRecommendationsByBook(recommendations, languages, num));
	}

}
