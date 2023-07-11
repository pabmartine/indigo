package com.martinia.indigo.book.application.recommendation;

import com.martinia.indigo.book.domain.ports.repositories.BookMongoRepository;
import com.martinia.indigo.book.domain.ports.usecases.recommendation.CountBookRecommendationsByUserUseCase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Service
@Transactional
public class CountBookRecommendationsByUserUseCaseImpl implements CountBookRecommendationsByUserUseCase {

	@Resource
	private BookMongoRepository bookMongoRepository;

	@Override
	public Long countRecommendationsByUser(String user) {
		return bookMongoRepository.countRecommendationsByUser(user);
	}

}
