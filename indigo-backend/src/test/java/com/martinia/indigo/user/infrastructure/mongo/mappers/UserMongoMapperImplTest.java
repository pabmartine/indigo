package com.martinia.indigo.user.infrastructure.mongo.mappers;

import com.martinia.indigo.user.domain.model.User;
import com.martinia.indigo.user.infrastructure.mongo.entities.UserMongoEntity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UserMongoMapperImplTest {

	private final UserMongoMapper userMongoMapper = new UserMongoMapperImpl();

	@Test
	public void testDomain2Entity_NullDomain_ReturnsNull() {
		// Given
		User domain = null;

		// When
		UserMongoEntity entity = userMongoMapper.domain2Entity(domain);

		// Then
		assertNull(entity);
	}

	@Test
	public void testDomain2Entity_NonNullDomain_ReturnsEntityWithMappedValues() {
		// Given
		User domain = new User();
		domain.setId("1");
		domain.setUsername("john_doe");
		domain.setPassword("password");
		domain.setKindle("my_kindle");
		domain.setRole("user");
		domain.setLanguage("en");
		List<String> languageBooks = new ArrayList<>();
		languageBooks.add("en");
		languageBooks.add("fr");
		domain.setLanguageBooks(languageBooks);
		List<String> favoriteBooks = new ArrayList<>();
		favoriteBooks.add("book1");
		favoriteBooks.add("book2");
		domain.setFavoriteBooks(favoriteBooks);
		List<String> favoriteAuthors = new ArrayList<>();
		favoriteAuthors.add("author1");
		favoriteAuthors.add("author2");
		domain.setFavoriteAuthors(favoriteAuthors);

		// When
		UserMongoEntity entity = userMongoMapper.domain2Entity(domain);

		// Then
		assertEquals(domain.getId(), entity.getId());
		assertEquals(domain.getUsername(), entity.getUsername());
		assertEquals(domain.getPassword(), entity.getPassword());
		assertEquals(domain.getKindle(), entity.getKindle());
		assertEquals(domain.getRole(), entity.getRole());
		assertEquals(domain.getLanguage(), entity.getLanguage());
		assertEquals(domain.getLanguageBooks(), entity.getLanguageBooks());
		assertEquals(domain.getFavoriteBooks(), entity.getFavoriteBooks());
		assertEquals(domain.getFavoriteAuthors(), entity.getFavoriteAuthors());
	}

	@Test
	public void testEntity2Domain_NullEntity_ReturnsNull() {
		// Given
		UserMongoEntity entity = null;

		// When
		User domain = userMongoMapper.entity2Domain(entity);

		// Then
		assertNull(domain);
	}

	@Test
	public void testEntity2Domain_NonNullEntity_ReturnsDomainWithMappedValues() {
		// Given
		UserMongoEntity entity = new UserMongoEntity();
		entity.setId("1");
		entity.setUsername("john_doe");
		entity.setPassword("password");
		entity.setKindle("my_kindle");
		entity.setRole("user");
		entity.setLanguage("en");
		List<String> languageBooks = new ArrayList<>();
		languageBooks.add("en");
		languageBooks.add("fr");
		entity.setLanguageBooks(languageBooks);
		List<String> favoriteBooks = new ArrayList<>();
		favoriteBooks.add("book1");
		favoriteBooks.add("book2");
		entity.setFavoriteBooks(favoriteBooks);
		List<String> favoriteAuthors = new ArrayList<>();
		favoriteAuthors.add("author1");
		favoriteAuthors.add("author2");
		entity.setFavoriteAuthors(favoriteAuthors);

		// When
		User domain = userMongoMapper.entity2Domain(entity);

		// Then
		assertEquals(entity.getId(), domain.getId());
		assertEquals(entity.getUsername(), domain.getUsername());
		assertEquals(entity.getPassword(), domain.getPassword());
		assertEquals(entity.getKindle(), domain.getKindle());
		assertEquals(entity.getRole(), domain.getRole());
		assertEquals(entity.getLanguage(), domain.getLanguage());
		assertEquals(entity.getLanguageBooks(), domain.getLanguageBooks());
		assertEquals(entity.getFavoriteBooks(), domain.getFavoriteBooks());
		assertEquals(entity.getFavoriteAuthors(), domain.getFavoriteAuthors());
	}

	@Test
	public void testDomain2Entity_NullDomains_ReturnsNull() {
		// Given
		List<User> domains = null;

		// When
		List<UserMongoEntity> entities = userMongoMapper.domain2Entity(domains);

		// Then
		assertNull(entities);
	}

	@Test
	public void testDomain2Entity_NonNullDomains_ReturnsListOfMappedEntities() {
		// Given
		List<User> domains = new ArrayList<>();
		User domain1 = new User();
		domain1.setId("1");
		User domain2 = new User();
		domain2.setId("2");
		domains.add(domain1);
		domains.add(domain2);

		// When
		List<UserMongoEntity> entities = userMongoMapper.domain2Entity(domains);

		// Then
		assertEquals(domains.size(), entities.size());
		for (int i = 0; i < domains.size(); i++) {
			assertEquals(domains.get(i).getId(), entities.get(i).getId());
		}
	}

	@Test
	public void testEntities2Domains_NullEntities_ReturnsNull() {
		// Given
		List<UserMongoEntity> entities = null;

		// When
		List<User> domains = userMongoMapper.entities2Domains(entities);

		// Then
		assertNull(domains);
	}

	@Test
	public void testEntities2Domains_NonNullEntities_ReturnsListOfMappedDomains() {
		// Given
		List<UserMongoEntity> entities = new ArrayList<>();
		UserMongoEntity entity1 = new UserMongoEntity();
		entity1.setId("1");
		UserMongoEntity entity2 = new UserMongoEntity();
		entity2.setId("2");
		entities.add(entity1);
		entities.add(entity2);

		// When
		List<User> domains = userMongoMapper.entities2Domains(entities);

		// Then
		assertEquals(entities.size(), domains.size());
		for (int i = 0; i < entities.size(); i++) {
			assertEquals(entities.get(i).getId(), domains.get(i).getId());
		}
	}
}
