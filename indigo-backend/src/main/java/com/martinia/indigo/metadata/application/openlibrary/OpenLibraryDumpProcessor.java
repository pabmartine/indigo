package com.martinia.indigo.metadata.application.openlibrary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.LongConsumer;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.martinia.indigo.metadata.domain.model.OpenLibraryIndexCancelledException;
import com.martinia.indigo.metadata.domain.ports.repositories.OpenLibraryEditionMappingRepository;
import com.martinia.indigo.metadata.domain.ports.repositories.OpenLibraryRatingRepository;
import com.martinia.indigo.metadata.infrastructure.mongo.entities.OpenLibraryEditionMappingMongoEntity;
import com.martinia.indigo.metadata.infrastructure.mongo.entities.OpenLibraryRatingMongoEntity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class OpenLibraryDumpProcessor {
	private static final int BATCH_SIZE = 1_000;
	private static final long PROGRESS_INTERVAL = 10_000L;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Resource
	private OpenLibraryEditionMappingRepository mappingRepository;

	@Resource
	private OpenLibraryRatingRepository ratingRepository;

	public EditionProcessingResult processEditions(final Path dump, final String indexVersion,
			final Set<String> libraryIsbns, final BooleanSupplier cancelled, final LongConsumer progress) {
		mappingRepository.deleteByIndexVersion(indexVersion);
		final List<OpenLibraryEditionMappingMongoEntity> batch = new ArrayList<>(BATCH_SIZE);
		final Set<String> matchedIsbns = new HashSet<>();
		final Set<String> matchedWorks = new HashSet<>();
		long processed = 0L;
		try (BufferedReader reader = gzipReader(dump)) {
			String line;
			while ((line = reader.readLine()) != null) {
				processed++;
				checkCancellation(cancelled);
				final String[] columns = line.split("\\t", 5);
				if (columns.length < 5) {
					reportProgress(progress, processed);
					continue;
				}
				final JsonNode edition = objectMapper.readTree(columns[4]);
				final String editionId = normalizeOpenLibraryId(edition.path("key").asText(columns[1]));
				final String workId = firstWorkId(edition.path("works"));
				if (editionId == null || workId == null) {
					reportProgress(progress, processed);
					continue;
				}
				for (String isbn : editionIsbns(edition)) {
					if (libraryIsbns.contains(isbn)) {
						matchedIsbns.add(isbn);
						matchedWorks.add(workId);
						batch.add(OpenLibraryEditionMappingMongoEntity.builder()
								.id(indexVersion + ':' + isbn + ':' + editionId)
								.indexVersion(indexVersion)
								.isbn(isbn)
								.editionId(editionId)
								.workId(workId)
								.build());
					}
				}
				if (batch.size() >= BATCH_SIZE) {
					mappingRepository.saveAll(new ArrayList<>(batch));
					batch.clear();
				}
				reportProgress(progress, processed);
			}
			if (!batch.isEmpty()) {
				mappingRepository.saveAll(new ArrayList<>(batch));
			}
			progress.accept(processed);
			return new EditionProcessingResult(processed, matchedIsbns, matchedWorks);
		}
		catch (IOException exception) {
			throw new IllegalStateException("Invalid or unreadable Open Library editions dump", exception);
		}
	}

	public RatingProcessingResult processRatings(final Path dump, final String indexVersion,
			final Set<String> workIds, final BooleanSupplier cancelled, final LongConsumer progress) {
		ratingRepository.deleteByIndexVersion(indexVersion);
		final Map<String, RatingAccumulator> ratings = new HashMap<>();
		long processed = 0L;
		try (BufferedReader reader = gzipReader(dump)) {
			String line;
			while ((line = reader.readLine()) != null) {
				processed++;
				checkCancellation(cancelled);
				final String[] columns = line.split("\\t", -1);
				if (columns.length < 3) {
					reportProgress(progress, processed);
					continue;
				}
				final String workId = normalizeOpenLibraryId(columns[0]);
				if (!workIds.contains(workId)) {
					reportProgress(progress, processed);
					continue;
				}
				try {
					final double rating = Double.parseDouble(columns[2]);
					ratings.computeIfAbsent(workId, ignored -> new RatingAccumulator()).add(rating);
				}
				catch (NumberFormatException ignored) {
					// The ratings dump can contain a header; malformed data rows are ignored.
				}
				reportProgress(progress, processed);
			}
			final List<OpenLibraryRatingMongoEntity> batch = new ArrayList<>(BATCH_SIZE);
			for (Map.Entry<String, RatingAccumulator> entry : ratings.entrySet()) {
				final RatingAccumulator value = entry.getValue();
				batch.add(OpenLibraryRatingMongoEntity.builder()
						.id(indexVersion + ':' + entry.getKey())
						.indexVersion(indexVersion)
						.workId(entry.getKey())
						.average((float) (value.sum / value.count))
						.count(value.count)
						.distribution(value.distribution)
						.build());
				if (batch.size() >= BATCH_SIZE) {
					ratingRepository.saveAll(new ArrayList<>(batch));
					batch.clear();
				}
			}
			if (!batch.isEmpty()) {
				ratingRepository.saveAll(new ArrayList<>(batch));
			}
			progress.accept(processed);
			return new RatingProcessingResult(processed, ratings.size());
		}
		catch (IOException exception) {
			throw new IllegalStateException("Invalid or unreadable Open Library ratings dump", exception);
		}
	}

	private BufferedReader gzipReader(final Path dump) throws IOException {
		final InputStream input = Files.newInputStream(dump);
		try {
			return new BufferedReader(new InputStreamReader(new GZIPInputStream(input), StandardCharsets.UTF_8));
		}
		catch (IOException exception) {
			input.close();
			throw exception;
		}
	}

	private Set<String> editionIsbns(final JsonNode edition) {
		final Set<String> result = new HashSet<>();
		addIsbns(result, edition.path("isbn_10"));
		addIsbns(result, edition.path("isbn_13"));
		addIsbns(result, edition.path("isbn"));
		return result;
	}

	private void addIsbns(final Set<String> target, final JsonNode values) {
		if (values.isArray()) {
			values.forEach(value -> {
				final String isbn = normalizeIsbn(value.asText());
				if (isbn != null) {
					target.add(isbn);
				}
			});
		}
	}

	private String firstWorkId(final JsonNode works) {
		if (!works.isArray() || works.isEmpty()) {
			return null;
		}
		return normalizeOpenLibraryId(works.get(0).path("key").asText(null));
	}

	private String normalizeIsbn(final String value) {
		if (value == null) {
			return null;
		}
		final String normalized = value.replaceAll("[^0-9Xx]", "").toUpperCase();
		return normalized.length() == 10 || normalized.length() == 13 ? normalized : null;
	}

	private String normalizeOpenLibraryId(final String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		final int separator = value.lastIndexOf('/');
		return separator >= 0 ? value.substring(separator + 1) : value;
	}

	private void checkCancellation(final BooleanSupplier cancelled) {
		if (cancelled.getAsBoolean()) {
			throw new OpenLibraryIndexCancelledException();
		}
	}

	private void reportProgress(final LongConsumer progress, final long processed) {
		if (processed % PROGRESS_INTERVAL == 0) {
			progress.accept(processed);
		}
	}

	public record EditionProcessingResult(long processedRecords, Set<String> matchedIsbns,
			Set<String> matchedWorks) {
	}

	public record RatingProcessingResult(long processedRecords, long worksWithRatings) {
	}

	private static final class RatingAccumulator {
		private double sum;
		private long count;
		private final Map<String, Long> distribution = new LinkedHashMap<>();

		private void add(final double rating) {
			sum += rating;
			count++;
			final String key = BigDecimal.valueOf(rating).stripTrailingZeros().toPlainString();
			distribution.merge(key, 1L, Long::sum);
		}
	}
}
