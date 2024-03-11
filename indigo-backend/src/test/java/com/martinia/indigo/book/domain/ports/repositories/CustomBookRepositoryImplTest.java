package com.martinia.indigo.book.domain.ports.repositories;

import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.domain.model.Search;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomBookRepositoryImplTest {

	@Mock
	private MongoTemplate mongoTemplate;

	@InjectMocks
	private CustomBookRepositoryImpl customBookRepository;

	public CustomBookRepositoryImplTest() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void count_WithSearchCriteria_ShouldReturnCount() {
		Search search = new Search();
		search.setPath("path");
		search.setTitle("title");
		search.setAuthor("author");
		search.setIni(Calendar.getInstance().getTime());
		search.setEnd(Calendar.getInstance().getTime());
		search.setMin(100);
		search.setMax(200);
		search.setSelectedTags(Arrays.asList("tag1", "tag2"));
		search.setSerie("serie");
		search.setLanguages(Arrays.asList("en", "es"));

		Query query = new Query();

		List<Criteria> criterias = new ArrayList<>();

		String[] pathTerms = search.getPath().split(" ");
		for (String term : pathTerms) {
			criterias.add(Criteria.where("path").regex(term, "i"));
		}

		String[] titleTerms = search.getTitle().split(" ");
		for (String term : titleTerms) {
			criterias.add(Criteria.where("title").regex(term, "i"));
		}

		String[] authorTerms = search.getAuthor().split(" ");
		for (String term : authorTerms) {
			criterias.add(Criteria.where("authors").regex(term, "i"));
		}

		criterias.add(Criteria.where("pubDate").gte(search.getIni()));

		Calendar cEnd = Calendar.getInstance();
		cEnd.setTime(search.getEnd());
		cEnd.set(Calendar.HOUR_OF_DAY, 23);
		cEnd.set(Calendar.MINUTE, 59);
		criterias.add(Criteria.where("pubDate").lte(cEnd.getTime()));

		criterias.add(Criteria.where("pages").gte(search.getMin()));
		criterias.add(Criteria.where("pages").lte(search.getMax()));

		criterias.add(Criteria.where("tags").in(search.getSelectedTags()));

		criterias.add(Criteria.where("serie.name").is(search.getSerie()));

		criterias.add(Criteria.where("languages").in(search.getLanguages()));

		query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[0])));

		long expectedCount = 10;
		when(mongoTemplate.count(query, BookMongoEntity.class)).thenReturn(expectedCount);

		long count = customBookRepository.count(search);

		assertEquals(expectedCount, count);
		verify(mongoTemplate).count(query, BookMongoEntity.class);
	}

	@Test
	void findAll_WithSearchCriteriaAndPageable_ShouldReturnBookList() {
		Search search = new Search();
		search.setPath("path");
		search.setTitle("title");
		search.setAuthor("author");
		search.setIni(Calendar.getInstance().getTime());
		search.setEnd(Calendar.getInstance().getTime());
		search.setMin(100);
		search.setMax(200);
		search.setSelectedTags(Arrays.asList("tag1", "tag2"));
		search.setSerie("serie");
		search.setLanguages(Arrays.asList("en", "es"));

		int page = 0;
		int size = 10;
		String sort = "title";
		String order = "asc";
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(order), sort));

		Query query = new Query().with(pageable);

		List<Criteria> criterias = new ArrayList<>();

		String[] pathTerms = search.getPath().split(" ");
		for (String term : pathTerms) {
			criterias.add(Criteria.where("path").regex(term, "i"));
		}

		String[] titleTerms = search.getTitle().split(" ");
		for (String term : titleTerms) {
			criterias.add(Criteria.where("title").regex(term, "i"));
		}

		String[] authorTerms = search.getAuthor().split(" ");
		for (String term : authorTerms) {
			criterias.add(Criteria.where("authors").regex(term, "i"));
		}

		criterias.add(Criteria.where("pubDate").gte(search.getIni()));

		Calendar cEnd = Calendar.getInstance();
		cEnd.setTime(search.getEnd());
		cEnd.set(Calendar.HOUR_OF_DAY, 23);
		cEnd.set(Calendar.MINUTE, 59);
		criterias.add(Criteria.where("pubDate").lte(cEnd.getTime()));

		criterias.add(Criteria.where("pages").gte(search.getMin()));
		criterias.add(Criteria.where("pages").lte(search.getMax()));

		criterias.add(Criteria.where("tags").in(search.getSelectedTags()));

		criterias.add(Criteria.where("serie.name").is(search.getSerie()));

		criterias.add(Criteria.where("languages").in(search.getLanguages()));

		query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[0])));

		List<BookMongoEntity> expectedBooks = new ArrayList<>();
		expectedBooks.add(new BookMongoEntity());
		expectedBooks.add(new BookMongoEntity());
		when(mongoTemplate.find(query, BookMongoEntity.class)).thenReturn(expectedBooks);

		List<BookMongoEntity> books = customBookRepository.findAll(search, page, size, sort, order);

		assertEquals(expectedBooks, books);
		verify(mongoTemplate).find(query, BookMongoEntity.class);
	}

	@Test
	void getRecommendationsByBook_WithBook_ShouldReturnRecommendations() {
		BookMongoEntity book = new BookMongoEntity();
		book.setId("bookId");
		book.setTags(Arrays.asList("tag1", "tag2"));
		book.setPubDate(Calendar.getInstance().getTime());
		book.setPages(200);

		Query query = new Query();

		List<Criteria> criterias = new ArrayList<>();

		criterias.add(Criteria.where("id").ne(book.getId()));
		criterias.add(Criteria.where("tags").all(book.getTags()));

		Calendar cIni = Calendar.getInstance();
		cIni.setTime(book.getPubDate());
		cIni.add(Calendar.YEAR, -5);
		criterias.add(Criteria.where("pubDate").gte(cIni.getTime()));

		Calendar cEnd = Calendar.getInstance();
		cEnd.setTime(book.getPubDate());
		cEnd.set(Calendar.HOUR_OF_DAY, 23);
		cEnd.set(Calendar.MINUTE, 59);
		cEnd.add(Calendar.YEAR, 5);
		criterias.add(Criteria.where("pubDate").lte(cEnd.getTime()));

		criterias.add(Criteria.where("pages").gte(book.getPages() - ((book.getPages() * 25) / 100)));
		criterias.add(Criteria.where("pages").lte(book.getPages() + ((book.getPages() * 25) / 100)));

		query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[0])));

		List<BookMongoEntity> expectedRecommendations = new ArrayList<>();
		expectedRecommendations.add(new BookMongoEntity());
		expectedRecommendations.add(new BookMongoEntity());
		when(mongoTemplate.find(query, BookMongoEntity.class)).thenReturn(expectedRecommendations);

		List<BookMongoEntity> recommendations = customBookRepository.getRecommendationsByBook(book);

		assertEquals(expectedRecommendations, recommendations);
		verify(mongoTemplate).find(query, BookMongoEntity.class);
	}

	@Test
	void getSerie_WithSerieAndLanguages_ShouldReturnBooksInSerie() {
		String serie = "Serie1";
		List<String> languages = Arrays.asList("en", "es");

		Query query = new Query();

		List<Criteria> criterias = new ArrayList<>();
		criterias.add(Criteria.where("serie.name").is(serie));
		criterias.add(Criteria.where("languages").in(languages));
		query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[0])));

		List<BookMongoEntity> expectedBooks = new ArrayList<>();
		expectedBooks.add(new BookMongoEntity());
		expectedBooks.add(new BookMongoEntity());
		when(mongoTemplate.find(query, BookMongoEntity.class)).thenReturn(expectedBooks);

		List<BookMongoEntity> books = customBookRepository.getSerie(serie, languages);

		assertEquals(expectedBooks, books);
		verify(mongoTemplate).find(query, BookMongoEntity.class);
	}

}
