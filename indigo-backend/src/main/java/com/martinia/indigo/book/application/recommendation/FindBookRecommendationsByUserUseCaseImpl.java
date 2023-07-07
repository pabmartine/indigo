package com.martinia.indigo.book.application.recommendation;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.repository.BookRepository;
import com.martinia.indigo.book.domain.service.recommendation.FindBookRecommendationsByUserUseCase;
import com.martinia.indigo.common.util.UtilComponent;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class FindBookRecommendationsByUserUseCaseImpl implements FindBookRecommendationsByUserUseCase {

	@Resource
	private BookRepository bookRepository;

	@Resource
	private UtilComponent utilComponent;

	@Override
	public List<Book> getRecommendationsByUser(String user, int page, int size, String sort, String order) {
		return bookRepository.getRecommendationsByUser(user, page, size, sort, order);
	}

}
