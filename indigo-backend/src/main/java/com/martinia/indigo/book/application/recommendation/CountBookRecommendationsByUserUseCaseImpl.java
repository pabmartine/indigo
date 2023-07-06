package com.martinia.indigo.book.application.recommendation;

import com.martinia.indigo.book.domain.repository.BookRepository;
import com.martinia.indigo.book.domain.service.recommendation.CountBookRecommendationsByUserUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Service
@Transactional
public class CountBookRecommendationsByUserUseCaseImpl implements CountBookRecommendationsByUserUseCase {

	@Resource
	private BookRepository bookRepository;

	@Override
	public Long countRecommendationsByUser(String user) {
		return bookRepository.countRecommendationsByUser(user);
	}

}
