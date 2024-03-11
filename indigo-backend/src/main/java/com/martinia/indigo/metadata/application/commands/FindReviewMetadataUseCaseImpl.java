package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.ReviewMongo;
import com.martinia.indigo.common.infrastructure.api.mappers.ReviewDtoMapper;
import com.martinia.indigo.common.infrastructure.api.model.ReviewDto;
import com.martinia.indigo.common.infrastructure.mongo.mappers.ReviewMongoMapper;
import com.martinia.indigo.metadata.domain.ports.adapters.amazon.FindAmazonReviewsPort;
import com.martinia.indigo.metadata.domain.ports.adapters.goodreads.FindGoodReadsReviewsPort;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.FindReviewMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class FindReviewMetadataUseCaseImpl implements FindReviewMetadataUseCase {

	@Resource
	private BookRepository bookRepository;

	@Resource
	private Optional<FindGoodReadsReviewsPort> findGoodReadsReviewsPort;

	@Resource
	private Optional<FindAmazonReviewsPort> findAmazonReviewsPort;

	@Resource
	private ReviewDtoMapper reviewDtoMapper;

	@Resource
	private ReviewMongoMapper reviewMongoMapper;

	@Override
	public void find(final String bookId, final boolean override, final long lastExecution, final String lang) {

		bookRepository.findById(bookId).ifPresent(book -> {

			if (override || refreshReviewMetadata(book.getReviews())) {
				log.debug("Obtaining reviews");
				List<ReviewDto> reviews = findGoodReadsReviewsPort.map(gr -> gr.getReviews(lang, book.getTitle(), book.getAuthors()))
						.orElse(Collections.EMPTY_LIST);

				if (CollectionUtils.isEmpty(reviews)) {
					reviews = findAmazonReviewsPort.map(amazon -> amazon.getReviews(book.getTitle(), book.getAuthors()))
							.orElse(Collections.EMPTY_LIST);
				}

				if (CollectionUtils.isEmpty(reviews)) {
					book.setReviews(null);
				}
				else {
					book.setReviews(reviewMongoMapper.domains2Entities(reviewDtoMapper.dtos2domains(reviews)));
					if (book.getRating() == 0) {
						book.setRating(
								book.getReviews().stream().map(ReviewMongo::getRating).reduce(0, Integer::sum) / book.getReviews().size());
					}
				}

				bookRepository.save(book);

			}
		});
	}

	private boolean refreshReviewMetadata(final List<ReviewMongo> reviews) {
		return CollectionUtils.isEmpty(reviews) || reviews.stream().anyMatch(review -> {
			LocalDateTime lastMetadataSync = Optional.ofNullable(review.getLastMetadataSync())
					.map(date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
					.orElse(LocalDateTime.now());
			LocalDateTime currentDate = LocalDateTime.now();
			LocalDateTime expirationDate = lastMetadataSync.plusDays(7);
			return expirationDate.isBefore(currentDate);
		});
	}

}
