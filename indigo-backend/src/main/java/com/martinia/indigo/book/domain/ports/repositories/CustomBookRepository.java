package com.martinia.indigo.book.domain.ports.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.martinia.indigo.book.domain.model.BookPageData;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.domain.model.Search;
import com.martinia.indigo.serie.domain.model.SeriePageData;

public interface CustomBookRepository {

	// Maintenance reads are bounded and exclude covers and reviews.
	List<BookMongoEntity> findCategoryBatch(String afterId);

	List<BookMongoEntity> findReconciliationBatch(String afterId);

	void updateReconciledMetadata(BookMongoEntity book, boolean tagsChanged, boolean identifiersChanged);

	void updateSimilar(String bookId, List<String> similar);

	void updateRecommendations(String bookId, List<String> recommendations);

	List<BookMongoEntity> findSimilarCandidates(Search search, int page);

	List<BookMongoEntity> findAll(Search search, int page, int size, String sort, String order);

	List<BookMongoEntity> findSummary(Search search, int page, int size, String sort, String order);

	BookPageData findSummaryPage(Search search, int page, int size, String sort, String order);

	BookPageData findAllPage(Search search, int page, int size, String sort, String order);

	long countBooks(Search search);

	long countReviews();

	Map<String, Long> getNumBooksBySerie(List<String> languages, int page, int size, String sort, String order);

	Long getNumSeries(List<String> languages);

	SeriePageData getSeriesPage(List<String> languages, int page, int size, String sort, String order);

	Optional<String> findFirstImageBySerie(String serie);

	List<BookMongoEntity> getSimilar(List<String> similar, List<String> languages);

	List<BookMongoEntity> getSerie(String serie, List<String> languages);

	List<BookMongoEntity> getRecommendationsByBook(BookMongoEntity book);

	List<BookMongoEntity> getRecommendationsByUser(String user, int page, int size, String sort, String order);

	List<BookMongoEntity> getRecommendationsByBook(List<String> recommendations, List<String> languages, int num);

	long countRecommendationsByUser(String user);

	List<String> getBookLanguages();
	void clearCache();
}
