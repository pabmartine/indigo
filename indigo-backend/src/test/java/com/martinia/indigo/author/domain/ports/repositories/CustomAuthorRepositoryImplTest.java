package com.martinia.indigo.author.domain.ports.repositories;

import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomAuthorRepositoryImplTest {

	@Mock
	private MongoTemplate mongoTemplate;

	@InjectMocks
	private CustomAuthorRepositoryImpl customAuthorRepository;

	public CustomAuthorRepositoryImplTest() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void count_WithLanguages_ShouldReturnCount() {
		List<String> languages = Arrays.asList("en", "es");

		Query query = new Query();
		List<Criteria> criterias = new ArrayList<>();
		for (String lang : languages) {
			criterias.add(Criteria.where("numBooks.languages." + lang).exists(true));
		}
		query.addCriteria(new Criteria().orOperator(criterias.toArray(new Criteria[0])));

		long expectedCount = 10;
		when(mongoTemplate.count(query, AuthorMongoEntity.class)).thenReturn(expectedCount);

		long count = customAuthorRepository.count(languages);

		assertEquals(expectedCount, count);
		verify(mongoTemplate).count(query, AuthorMongoEntity.class);
	}

	@Test
	void findAll_WithLanguagesAndPageable_ShouldReturnAuthorList() {
		List<String> languages = Arrays.asList("en", "es");
		int page = 0;
		int size = 10;
		Pageable pageable = PageRequest.of(page, size);

		Query query = new Query().with(pageable);
		List<Criteria> criterias = new ArrayList<>();
		for (String lang : languages) {
			criterias.add(Criteria.where("numBooks.languages." + lang).exists(true));
		}
		query.addCriteria(new Criteria().orOperator(criterias.toArray(new Criteria[0])));

		List<AuthorMongoEntity> expectedAuthors = new ArrayList<>();
		expectedAuthors.add(new AuthorMongoEntity());
		expectedAuthors.add(new AuthorMongoEntity());
		when(mongoTemplate.find(query, AuthorMongoEntity.class)).thenReturn(expectedAuthors);

		List<AuthorMongoEntity> authors = customAuthorRepository.findAll(languages, pageable);

		assertEquals(expectedAuthors, authors);
		verify(mongoTemplate).find(query, AuthorMongoEntity.class);
	}
}
