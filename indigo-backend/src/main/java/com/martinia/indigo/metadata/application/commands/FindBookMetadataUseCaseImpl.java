package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.metadata.domain.model.events.BookMetadataFoundEvent;
import com.martinia.indigo.metadata.domain.model.BookMetadataResult;
import com.martinia.indigo.metadata.domain.model.BookMetadataQuery;
import com.martinia.indigo.metadata.domain.model.DynamicMetadataPolicy;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import com.martinia.indigo.metadata.domain.model.MetadataMergePolicy;
import com.martinia.indigo.metadata.domain.ports.adapters.google.FindGoogleBooksBookPort;
import com.martinia.indigo.metadata.domain.ports.adapters.openlibrary.FindOpenLibraryBookPort;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.FindBookMetadataUseCase;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
public class FindBookMetadataUseCaseImpl implements FindBookMetadataUseCase {
	private static final int RATING_MAX_AGE_DAYS = 7;

	@Resource
	protected BookRepository bookRepository;

	@Resource
	private Optional<FindGoogleBooksBookPort> findGoogleBooksBookPort;

	@Resource
	private Optional<FindOpenLibraryBookPort> findOpenLibraryBookPort;

	@Resource
	private EventBus eventBus;

	@Override
	public MetadataItemResult find(final String bookId, final MetadataMergePolicy mergePolicy,
			final DynamicMetadataPolicy dynamicPolicy, final long lastExecution) {

		return bookRepository.findById(bookId).map(book -> {

			if (mergePolicy != MetadataMergePolicy.FILL_MISSING
					|| dynamicPolicy != DynamicMetadataPolicy.REFRESH_IF_STALE) {
				throw new IllegalArgumentException("Unsupported book metadata policy");
			}

			if (!refreshDynamicMetadata(book)) {
				return MetadataItemResult.SKIPPED;
			}

			BookMetadataResult bookData = null;
			boolean providerSucceeded = false;
			boolean providerAttempted = false;
			final BookMetadataQuery query = BookMetadataQuery.builder()
					.title(book.getTitle())
					.authors(book.getAuthors())
					.isbn10(book.getIsbn10())
					.isbn13(book.getIsbn13())
					.languages(book.getLanguages())
					.publicationYear(book.getPubDate() == null ? null : book.getPubDate().toInstant()
							.atZone(ZoneId.systemDefault()).getYear())
					.build();
			final boolean hasIsbn = query.preferredIsbn() != null;

			if (hasIsbn && findOpenLibraryBookPort.isPresent()) {
				providerAttempted = true;
				try {
					bookData = findOpenLibraryBookPort.get().findBook(query);
					providerSucceeded = true;
				}
				catch (RuntimeException exception) {
					log.warn("Open Library ISBN lookup failed for {}: {}", book.getTitle(), exception.getMessage());
                    com.martinia.indigo.metadata.application.ProviderDiagnostics.record("OPEN_LIBRARY", "Obtener libro", exception);
				}
			}
			if ((bookData == null || bookData.getRatingAverage() == null) && findGoogleBooksBookPort.isPresent()) {
				providerAttempted = true;
				try {
					bookData = merge(bookData, findGoogleBooksBookPort.get().findBook(query));
					providerSucceeded = true;
				}
				catch (RuntimeException exception) {
					log.warn("Google Books failed for {}: {}", book.getTitle(), exception.getMessage());
                    com.martinia.indigo.metadata.application.ProviderDiagnostics.record("GOOGLE_BOOKS", "Obtener libro", exception);
				}
			}
			if (bookData == null && !hasIsbn && findOpenLibraryBookPort.isPresent()) {
				providerAttempted = true;
				try {
					bookData = findOpenLibraryBookPort.get().findBook(query);
					providerSucceeded = true;
				}
				catch (RuntimeException exception) {
					log.warn("Open Library failed for {}: {}", book.getTitle(), exception.getMessage());
                    com.martinia.indigo.metadata.application.ProviderDiagnostics.record("OPEN_LIBRARY", "Obtener libro", exception);
				}
			}

			if (bookData != null) {
				final Date now = Calendar.getInstance().getTime();
				if (bookData.getRatingAverage() != null) {
					book.setRatingAverage(bookData.getRatingAverage());
					book.setRatingsCount(bookData.getRatingsCount());
					book.setRatingDistribution(bookData.getRatingDistribution());
					book.setRatingProvider(bookData.getProvider());
					book.setRatingUpdatedAt(now);
					book.setRating(bookData.getRatingAverage());
				}
				if (bookData.getOpenLibraryWorkId() != null) {
					book.setOpenLibraryWorkId(bookData.getOpenLibraryWorkId());
				}
				if (bookData.getOpenLibraryEditionId() != null) {
					book.setOpenLibraryEditionId(bookData.getOpenLibraryEditionId());
				}
				book.setMetadataMatchStatus("MATCHED");
				book.setMetadataMatchConfidence(bookData.getMatchConfidence());
				// Keep the legacy fields populated until all API consumers have migrated.
				book.setProvider(bookData.getProvider());
				log.info("Found {} metadata for {}", bookData.getProvider(), book.getTitle());
			}
			else if (!providerSucceeded) {
				log.warn("No metadata provider completed successfully for {} (provider configured: {})",
						book.getTitle(), providerAttempted);
				return MetadataItemResult.ERROR;
			}
			else {
				log.info("No external metadata match found for {}", book.getTitle());
				book.setMetadataMatchStatus("NO_MATCH");
				book.setMetadataMatchConfidence(null);
			}

			book.setLastMetadataSync(Calendar.getInstance().getTime());
			bookRepository.save(book);

			eventBus.publish(BookMetadataFoundEvent.builder().bookId(book.getId()).similar(null).build());
			return bookData == null ? MetadataItemResult.NOT_FOUND : MetadataItemResult.FOUND;
		}).orElse(MetadataItemResult.SKIPPED);
	}

	private boolean refreshDynamicMetadata(final BookMongoEntity book) {
		if ("NO_MATCH".equals(book.getMetadataMatchStatus())) {
			return false;
		}
		return book.getRatingUpdatedAt() == null || book.getRatingUpdatedAt()
						.toInstant()
						.atZone(ZoneId.systemDefault())
						.toLocalDateTime()
						.plusDays(RATING_MAX_AGE_DAYS)
						.isBefore(Calendar.getInstance().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
	}

	private BookMetadataResult merge(final BookMetadataResult identityResult,
			final BookMetadataResult ratingResult) {
		if (identityResult == null) {
			return ratingResult;
		}
		if (ratingResult == null) {
			return identityResult;
		}
		return BookMetadataResult.builder()
				.ratingAverage(ratingResult.getRatingAverage())
				.ratingsCount(ratingResult.getRatingsCount())
				.ratingDistribution(ratingResult.getRatingDistribution())
				.provider(ratingResult.getProvider())
				.openLibraryWorkId(identityResult.getOpenLibraryWorkId())
				.openLibraryEditionId(identityResult.getOpenLibraryEditionId())
				.matchConfidence(minimumConfidence(identityResult.getMatchConfidence(), ratingResult.getMatchConfidence()))
				.build();
	}

	private Double minimumConfidence(final Double first, final Double second) {
		if (first == null) {
			return second;
		}
		if (second == null) {
			return first;
		}
		return Math.min(first, second);
	}
}
