package com.martinia.indigo.book.application.recommendation;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.repository.BookRepository;
import com.martinia.indigo.book.domain.service.recommendation.FindBookRecommendationsByBookUseCase;
import com.martinia.indigo.common.configuration.domain.repository.ConfigurationRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class FindBookRecommendationsByBookUseCaseImpl implements FindBookRecommendationsByBookUseCase {

	@Resource
	private BookRepository bookRepository;

	@Resource
	private ConfigurationRepository configurationRepository;

	@Override
	public List<Book> getRecommendationsByBook(List<String> recommendations, List<String> languages) {
		int num = configurationRepository.findByKey("books.recommendations").map(rec -> Integer.parseInt(rec.getValue())).orElse(0);
		return bookRepository.getRecommendationsByBook(recommendations, languages, num);
	}

}
