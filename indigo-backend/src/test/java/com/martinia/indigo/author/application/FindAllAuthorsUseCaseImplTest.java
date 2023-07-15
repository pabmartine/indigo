package com.martinia.indigo.author.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.domain.ports.usecases.FindAllAuthorsUseCase;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.author.infrastructure.mongo.mappers.AuthorMongoMapper;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class FindAllAuthorsUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindAllAuthorsUseCase findAllAuthorsUseCase;

	@MockBean
	private AuthorRepository authorRepository;

	@MockBean
	private AuthorMongoMapper authorMongoMapper;

	@Value("${data.author.default-image}")
	private String defaultImage;

	@Test
	public void testFindAllAuthors() {
		// Given
		List<String> languages = Arrays.asList("English", "Spanish");
		int page = 0;
		int size = 10;
		String sort = "name";
		String order = "asc";

		AuthorMongoEntity author1 = new AuthorMongoEntity();
		author1.setId("1");
		author1.setName("John Doe");
		author1.setImage(defaultImage);
		author1.setNumBooks(NumBooksMongo.builder().total(2).languages(Collections.singletonMap("English", 1)).build());

		AuthorMongoEntity author2 = new AuthorMongoEntity();
		author2.setId("2");
		author2.setName("Jane Smith");
		author2.setImage(defaultImage);
		author2.setNumBooks(NumBooksMongo.builder().total(2).languages(Collections.singletonMap("Spanish", 1)).build());

		List<AuthorMongoEntity> authorEntities = Arrays.asList(author1, author2);
		when(authorRepository.findAll(eq(languages), any(PageRequest.class))).thenReturn(authorEntities);

		when(authorMongoMapper.entities2Domains(authorEntities)).thenReturn(
				Arrays.asList(new Author(),
						new Author()));

		// When
		List<Author> authors = findAllAuthorsUseCase.findAll(languages, page, size, sort, order);

		// Then
		assertEquals(2, authors.size());
		// Assert other expectations
	}
}
