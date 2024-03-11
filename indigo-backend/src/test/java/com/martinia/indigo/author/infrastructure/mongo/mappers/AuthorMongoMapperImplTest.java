package com.martinia.indigo.author.infrastructure.mongo.mappers;

import com.martinia.indigo.author.domain.model.Author;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.common.model.NumBooks;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AuthorMongoMapperImplTest {

	private final AuthorMongoMapper authorMongoMapper = new AuthorMongoMapperImpl();

	@Test
	public void testDomain2Entity_NullDomain_ReturnsNull() {
		// Given
		Author domain = null;

		// When
		AuthorMongoEntity entity = authorMongoMapper.domain2Entity(domain);

		// Then
		assertNull(entity);
	}

	@Test
	public void testDomain2Entity_NonNullDomain_ReturnsEntityWithMappedValues() {
		// Given
		Author domain = new Author();
		domain.setId("1");
		domain.setName("John Doe");
		domain.setSort("Doe, John");
		domain.setDescription("Author description");
		domain.setProvider("Provider");
		domain.setImage("image.jpg");
		NumBooks numBooks = new NumBooks();
		numBooks.setTotal(10);
		Map<String, Integer> languages = new HashMap<>();
		languages.put("English", 5);
		languages.put("Spanish", 3);
		numBooks.setLanguages(languages);
		domain.setNumBooks(numBooks);

		// When
		AuthorMongoEntity entity = authorMongoMapper.domain2Entity(domain);

		// Then
		assertEquals(domain.getId(), entity.getId());
		assertEquals(domain.getName(), entity.getName());
		assertEquals(domain.getSort(), entity.getSort());
		assertEquals(domain.getDescription(), entity.getDescription());
		assertEquals(domain.getProvider(), entity.getProvider());
		assertEquals(domain.getImage(), entity.getImage());
		assertEquals(domain.getLastMetadataSync(), entity.getLastMetadataSync());
		assertEquals(domain.getNumBooks().getTotal(), entity.getNumBooks().getTotal());
		assertEquals(domain.getNumBooks().getLanguages().size(), entity.getNumBooks().getLanguages().size());
		assertEquals(domain.getNumBooks().getLanguages().get("English"), entity.getNumBooks().getLanguages().get("English"));
		assertEquals(domain.getNumBooks().getLanguages().get("Spanish"), entity.getNumBooks().getLanguages().get("Spanish"));
	}

	@Test
	public void testEntity2Domain_NullEntity_ReturnsNull() {
		// Given
		AuthorMongoEntity entity = null;

		// When
		Author domain = authorMongoMapper.entity2Domain(entity);

		// Then
		assertNull(domain);
	}

	@Test
	public void testEntity2Domain_NonNullEntity_ReturnsDomainWithMappedValues() {
		// Given
		AuthorMongoEntity entity = new AuthorMongoEntity();
		entity.setId("1");
		entity.setName("John Doe");
		entity.setSort("Doe, John");
		entity.setDescription("Author description");
		entity.setProvider("Provider");
		entity.setImage("image.jpg");
		NumBooksMongo numBooks = new NumBooksMongo();
		numBooks.setTotal(10);
		Map<String, Integer> languages = new HashMap<>();
		languages.put("English", 5);
		languages.put("Spanish", 3);
		numBooks.setLanguages(languages);
		entity.setNumBooks(numBooks);

		// When
		Author domain = authorMongoMapper.entity2Domain(entity);

		// Then
		assertEquals(entity.getId(), domain.getId());
		assertEquals(entity.getName(), domain.getName());
		assertEquals(entity.getSort(), domain.getSort());
		assertEquals(entity.getDescription(), domain.getDescription());
		assertEquals(entity.getProvider(), domain.getProvider());
		assertEquals(entity.getImage(), domain.getImage());
		assertEquals(entity.getLastMetadataSync(), domain.getLastMetadataSync());
		assertEquals(entity.getNumBooks().getTotal(), domain.getNumBooks().getTotal());
		assertEquals(entity.getNumBooks().getLanguages().size(), domain.getNumBooks().getLanguages().size());
		assertEquals(entity.getNumBooks().getLanguages().get("English"), domain.getNumBooks().getLanguages().get("English"));
		assertEquals(entity.getNumBooks().getLanguages().get("Spanish"), domain.getNumBooks().getLanguages().get("Spanish"));
	}

	@Test
	public void testDomain2Entities_NullDomain_ReturnsNull() {
		// Given
		List<Author> domain = null;

		// When
		List<AuthorMongoEntity> entities = authorMongoMapper.domain2Entity(domain);

		// Then
		assertNull(entities);
	}

	@Test
	public void testDomain2Entities_NonNullDomain_ReturnsListOfMappedEntities() {
		// Given
		List<Author> domain = new ArrayList<>();
		Author author1 = new Author();
		author1.setId("1");
		author1.setName("John Doe");
		author1.setSort("Doe, John");
		author1.setDescription("Author description");
		author1.setProvider("Provider");
		author1.setImage("image.jpg");
		NumBooks numBooks1 = new NumBooks();
		numBooks1.setTotal(10);
		Map<String, Integer> languages1 = new HashMap<>();
		languages1.put("English", 5);
		languages1.put("Spanish", 3);
		numBooks1.setLanguages(languages1);
		author1.setNumBooks(numBooks1);
		Author author2 = new Author();
		author2.setId("2");
		author2.setName("Jane Smith");
		author2.setSort("Smith, Jane");
		author2.setDescription("Author description");
		author2.setProvider("Provider");
		author2.setImage("image.jpg");
		NumBooks numBooks2 = new NumBooks();
		numBooks2.setTotal(15);
		Map<String, Integer> languages2 = new HashMap<>();
		languages2.put("English", 10);
		languages2.put("French", 5);
		numBooks2.setLanguages(languages2);
		author2.setNumBooks(numBooks2);
		domain.add(author1);
		domain.add(author2);

		// When
		List<AuthorMongoEntity> entities = authorMongoMapper.domain2Entity(domain);

		// Then
		assertEquals(domain.size(), entities.size());
		for (int i = 0; i < domain.size(); i++) {
			assertEquals(domain.get(i).getId(), entities.get(i).getId());
			assertEquals(domain.get(i).getName(), entities.get(i).getName());
			assertEquals(domain.get(i).getSort(), entities.get(i).getSort());
			assertEquals(domain.get(i).getDescription(), entities.get(i).getDescription());
			assertEquals(domain.get(i).getProvider(), entities.get(i).getProvider());
			assertEquals(domain.get(i).getImage(), entities.get(i).getImage());
			assertEquals(domain.get(i).getLastMetadataSync(), entities.get(i).getLastMetadataSync());
			assertEquals(domain.get(i).getNumBooks().getTotal(), entities.get(i).getNumBooks().getTotal());
			assertEquals(domain.get(i).getNumBooks().getLanguages().size(), entities.get(i).getNumBooks().getLanguages().size());
			assertEquals(domain.get(i).getNumBooks().getLanguages().get("English"),
					entities.get(i).getNumBooks().getLanguages().get("English"));
			assertEquals(domain.get(i).getNumBooks().getLanguages().get("Spanish"),
					entities.get(i).getNumBooks().getLanguages().get("Spanish"));
		}
	}

	@Test
	public void testEntities2Domains_NullEntities_ReturnsNull() {
		// Given
		List<AuthorMongoEntity> entities = null;

		// When
		List<Author> domains = authorMongoMapper.entities2Domains(entities);

		// Then
		assertNull(domains);
	}

	@Test
	public void testEntities2Domains_NonNullEntities_ReturnsListOfMappedDomains() {
		// Given
		List<AuthorMongoEntity> entities = new ArrayList<>();
		AuthorMongoEntity entity1 = new AuthorMongoEntity();
		entity1.setId("1");
		entity1.setName("John Doe");
		entity1.setSort("Doe, John");
		entity1.setDescription("Author description");
		entity1.setProvider("Provider");
		entity1.setImage("image.jpg");
		NumBooksMongo numBooks1 = new NumBooksMongo();
		numBooks1.setTotal(10);
		Map<String, Integer> languages1 = new HashMap<>();
		languages1.put("English", 5);
		languages1.put("Spanish", 3);
		numBooks1.setLanguages(languages1);
		entity1.setNumBooks(numBooks1);
		AuthorMongoEntity entity2 = new AuthorMongoEntity();
		entity2.setId("2");
		entity2.setName("Jane Smith");
		entity2.setSort("Smith, Jane");
		entity2.setDescription("Author description");
		entity2.setProvider("Provider");
		entity2.setImage("image.jpg");
		NumBooksMongo numBooks2 = new NumBooksMongo();
		numBooks2.setTotal(15);
		Map<String, Integer> languages2 = new HashMap<>();
		languages2.put("English", 10);
		languages2.put("French", 5);
		numBooks2.setLanguages(languages2);
		entity2.setNumBooks(numBooks2);
		entities.add(entity1);
		entities.add(entity2);

		// When
		List<Author> domains = authorMongoMapper.entities2Domains(entities);

		// Then
		assertEquals(entities.size(), domains.size());
		for (int i = 0; i < entities.size(); i++) {
			assertEquals(entities.get(i).getId(), domains.get(i).getId());
			assertEquals(entities.get(i).getName(), domains.get(i).getName());
			assertEquals(entities.get(i).getSort(), domains.get(i).getSort());
			assertEquals(entities.get(i).getDescription(), domains.get(i).getDescription());
			assertEquals(entities.get(i).getProvider(), domains.get(i).getProvider());
			assertEquals(entities.get(i).getImage(), domains.get(i).getImage());
			assertEquals(entities.get(i).getLastMetadataSync(), domains.get(i).getLastMetadataSync());
			assertEquals(entities.get(i).getNumBooks().getTotal(), domains.get(i).getNumBooks().getTotal());
			assertEquals(entities.get(i).getNumBooks().getLanguages().size(), domains.get(i).getNumBooks().getLanguages().size());
			assertEquals(entities.get(i).getNumBooks().getLanguages().get("English"),
					domains.get(i).getNumBooks().getLanguages().get("English"));
			assertEquals(entities.get(i).getNumBooks().getLanguages().get("Spanish"),
					domains.get(i).getNumBooks().getLanguages().get("Spanish"));
		}
	}
}
