package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.ReviewMongo;
import com.martinia.indigo.common.infrastructure.api.mappers.ReviewDtoMapper;
import com.martinia.indigo.common.infrastructure.api.model.ReviewDto;
import com.martinia.indigo.common.infrastructure.mongo.mappers.ReviewMongoMapper;
import com.martinia.indigo.metadata.domain.ports.adapters.amazon.FindAmazonReviewsPort;
import com.martinia.indigo.metadata.domain.ports.adapters.goodreads.FindGoodReadsReviewsPort;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.FindReviewMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
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
	public MetadataItemResult find(final String bookId, final boolean override, final String lang) {

		return bookRepository.findById(bookId).map(book -> {

			if (!override && !refreshReviewMetadata(book)) {
				return MetadataItemResult.SKIPPED;
			}

			List<ReviewDto> reviews = Collections.emptyList();
			String error = null;
			boolean providerSucceeded = false;
			if (findGoodReadsReviewsPort.isPresent()) {
				try {
					reviews = findGoodReadsReviewsPort.get().getReviews(lang, book.getTitle(), book.getAuthors());
					providerSucceeded = true;
				}
				catch (RuntimeException exception) {
					com.martinia.indigo.metadata.application.reviews.ReviewQueueService.rethrowCancellation(exception);
					error = com.martinia.indigo.metadata.application.ProviderDiagnostics.record("GOODREADS", "Obtener reseñas", exception);
					log.warn("Goodreads reviews failed for {}", book.getTitle(), exception);
				}
			}

			if (CollectionUtils.isEmpty(reviews) && findAmazonReviewsPort.isPresent()) {
				try {
					reviews = findAmazonReviewsPort.get().getReviews(book.getTitle(), book.getAuthors());
					providerSucceeded = true;
				}
				catch (RuntimeException exception) {
					com.martinia.indigo.metadata.application.reviews.ReviewQueueService.rethrowCancellation(exception);
					error = (error == null ? "" : error + "; ") + com.martinia.indigo.metadata.application.ProviderDiagnostics.record("AMAZON", "Obtener reseñas", exception);
					log.warn("Amazon reviews failed for {}", book.getTitle(), exception);
				}
			}

			if (!CollectionUtils.isEmpty(reviews)) {
				book.setReviews(mergeReviews(book.getReviews(),
						reviewMongoMapper.domains2Entities(reviewDtoMapper.dtos2domains(reviews))));
			}

			final MetadataItemResult result = !CollectionUtils.isEmpty(reviews) ? MetadataItemResult.FOUND
					: error == null && providerSucceeded ? MetadataItemResult.NOT_FOUND : MetadataItemResult.ERROR;
			if (result != MetadataItemResult.ERROR) {
				book.setLastReviewsMetadataSync(new Date());
			}
			book.setReviewsMetadataStatus(result.name());
			book.setReviewsMetadataError(error);
			bookRepository.save(book);
			return result;
		}).orElse(MetadataItemResult.SKIPPED);
	}

	private boolean refreshReviewMetadata(final com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity book) {
		return Optional.ofNullable(book.getLastReviewsMetadataSync())
					.map(date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
					.orElse(LocalDateTime.MIN).plusDays(7).isBefore(LocalDateTime.now());
	}

	private List<ReviewMongo> mergeReviews(final List<ReviewMongo> existing, final List<ReviewMongo> fetched) {
		final Map<String, ReviewMongo> unique = new LinkedHashMap<>();
		for (ReviewMongo review : Optional.ofNullable(existing).orElse(Collections.emptyList())) {
			unique.put(reviewKey(review), review);
		}
		for (ReviewMongo review : fetched) {
			unique.put(reviewKey(review), review);
		}
		return new ArrayList<>(unique.values());
	}

	private String reviewKey(final ReviewMongo review) {
		return String.join("|", Optional.ofNullable(review.getProvider()).orElse(""), Optional.ofNullable(review.getName()).orElse("").trim().toLowerCase(),
				String.valueOf(review.getDate()), String.valueOf(review.getRating()));
	}

}
