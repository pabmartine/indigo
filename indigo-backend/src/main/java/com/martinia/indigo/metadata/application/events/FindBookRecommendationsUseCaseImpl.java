package com.martinia.indigo.metadata.application.events;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.metadata.domain.ports.usecases.events.FindBookRecommendationsUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FindBookRecommendationsUseCaseImpl implements FindBookRecommendationsUseCase {

	@Resource
	protected BookRepository bookRepository;

	@Override
	public void findBookRecommendations(final String bookId) {

		bookRepository.findById(bookId).ifPresent(book -> {

			List<BookMongoEntity> recommendations = bookRepository.getRecommendationsByBook(book);
			if (!CollectionUtils.isEmpty(recommendations)) {
				book.setRecommendations(new ArrayList<>());
				recommendations.forEach(b -> book.getRecommendations().add(b.getId()));
				bookRepository.save(book);

				log.info("Found book recommendations for {}", book.getTitle());

			}

		});

	}
}
