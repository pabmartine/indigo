package com.martinia.indigo.author.infrastructure.api.mappers;

import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.infrastructure.api.model.AuthorDto;
import com.martinia.indigo.common.model.NumBooks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class AuthorDtoMapperImplTest {

	private AuthorDtoMapper authorDtoMapper;

	@BeforeEach
	public void setUp() {
		authorDtoMapper = new AuthorDtoMapperImpl();
	}

	@Test
	public void testDomain2Dto_withValidAuthor_shouldMapCorrectly() {
		// Arrange
		Author author = new Author();
		author.setId("1");
		author.setName("John Smith");
		author.setSort("Smith, John");
		author.setDescription("An amazing author");
		author.setProvider("Provider");
		author.setImage("image.jpg");

		NumBooks numBooks = new NumBooks();
		numBooks.setTotal(10);
		author.setNumBooks(numBooks);

		// Act
		AuthorDto authorDto = authorDtoMapper.domain2Dto(author);

		// Assert
		Assertions.assertEquals(author.getId(), authorDto.getId());
		Assertions.assertEquals(author.getName(), authorDto.getName());
		Assertions.assertEquals(author.getSort(), authorDto.getSort());
		Assertions.assertEquals(author.getDescription(), authorDto.getDescription());
		Assertions.assertEquals(author.getProvider(), authorDto.getProvider());
		Assertions.assertEquals(author.getImage(), authorDto.getImage());
		Assertions.assertEquals(author.getNumBooks().getTotal(), authorDto.getNumBooks());
	}

	@Test
	public void testDomain2Dto_withNullAuthor_shouldReturnNull() {
		// Act
		AuthorDto authorDto = authorDtoMapper.domain2Dto(null);

		// Assert
		Assertions.assertNull(authorDto);
	}

	@Test
	public void testDomains2Dtos_withValidAuthors_shouldMapCorrectly() {
		// Arrange
		List<Author> authors = new ArrayList<>();
		Author author1 = new Author();
		author1.setId("1");
		author1.setName("John Smith");
		author1.setSort("Smith, John");
		author1.setDescription("An amazing author");
		author1.setProvider("Provider");
		author1.setImage("image1.jpg");
		NumBooks numBooks1 = new NumBooks();
		numBooks1.setTotal(10);
		author1.setNumBooks(numBooks1);
		authors.add(author1);

		Author author2 = new Author();
		author2.setId("2");
		author2.setName("Jane Doe");
		author2.setSort("Doe, Jane");
		author2.setDescription("A talented writer");
		author2.setProvider("Provider");
		author2.setImage("image2.jpg");
		NumBooks numBooks2 = new NumBooks();
		numBooks2.setTotal(5);
		author2.setNumBooks(numBooks2);
		authors.add(author2);

		// Act
		List<AuthorDto> authorDtos = authorDtoMapper.domains2Dtos(authors);

		// Assert
		Assertions.assertEquals(authors.size(), authorDtos.size());

		AuthorDto authorDto1 = authorDtos.get(0);
		Assertions.assertEquals(author1.getId(), authorDto1.getId());
		Assertions.assertEquals(author1.getName(), authorDto1.getName());
		Assertions.assertEquals(author1.getSort(), authorDto1.getSort());
		Assertions.assertEquals(author1.getDescription(), authorDto1.getDescription());
		Assertions.assertEquals(author1.getProvider(), authorDto1.getProvider());
		Assertions.assertEquals(author1.getImage(), authorDto1.getImage());
		Assertions.assertEquals(author1.getNumBooks().getTotal(), authorDto1.getNumBooks());

		AuthorDto authorDto2 = authorDtos.get(1);
		Assertions.assertEquals(author2.getId(), authorDto2.getId());
		Assertions.assertEquals(author2.getName(), authorDto2.getName());
		Assertions.assertEquals(author2.getSort(), authorDto2.getSort());
		Assertions.assertEquals(author2.getDescription(), authorDto2.getDescription());
		Assertions.assertEquals(author2.getProvider(), authorDto2.getProvider());
		Assertions.assertEquals(author2.getImage(), authorDto2.getImage());
		Assertions.assertEquals(author2.getNumBooks().getTotal(), authorDto2.getNumBooks());
	}

	@Test
	public void testDomains2Dtos_withNullAuthors_shouldReturnNull() {
		// Act
		List<AuthorDto> authorDtos = authorDtoMapper.domains2Dtos(null);

		// Assert
		Assertions.assertNull(authorDtos);
	}
}
