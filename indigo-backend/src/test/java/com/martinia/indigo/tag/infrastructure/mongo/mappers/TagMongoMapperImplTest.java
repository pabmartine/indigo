package com.martinia.indigo.tag.infrastructure.mongo.mappers;

import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.common.model.NumBooks;
import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TagMongoMapperImplTest {

	private final TagMongoMapper tagMongoMapper = new TagMongoMapperImpl();

	@Test
	public void testDomain2Entity_NullDomain_ReturnsNull() {
		// Given
		Tag domain = null;

		// When
		TagMongoEntity entity = tagMongoMapper.domain2Entity(domain);

		// Then
		assertNull(entity);
	}

	@Test
	public void testDomain2Entity_NonNullDomain_ReturnsEntityWithMappedValues() {
		// Given
		Tag domain = new Tag();
		domain.setId("1");
		domain.setName("fiction");
		domain.setImage("fiction.jpg");
		NumBooks numBooks = new NumBooks();
		numBooks.setTotal(100);
		Map<String, Integer> languages = new HashMap<>();
		languages.put("en", 50);
		languages.put("es", 50);
		numBooks.setLanguages(languages);
		domain.setNumBooks(numBooks);

		// When
		TagMongoEntity entity = tagMongoMapper.domain2Entity(domain);

		// Then
		assertEquals(domain.getId(), entity.getId());
		assertEquals(domain.getName(), entity.getName());
		assertEquals(domain.getImage(), entity.getImage());
		assertEquals(domain.getNumBooks().getTotal(), entity.getNumBooks().getTotal());
		assertEquals(domain.getNumBooks().getLanguages().get("en"), entity.getNumBooks().getLanguages().get("en"));
		assertEquals(domain.getNumBooks().getLanguages().get("es"), entity.getNumBooks().getLanguages().get("es"));
	}

	@Test
	public void testEntity2Domain_NullEntity_ReturnsNull() {
		// Given
		TagMongoEntity entity = null;

		// When
		Tag domain = tagMongoMapper.entity2Domain(entity);

		// Then
		assertNull(domain);
	}

	@Test
	public void testEntity2Domain_NonNullEntity_ReturnsDomainWithMappedValues() {
		// Given
		TagMongoEntity entity = new TagMongoEntity();
		entity.setId("1");
		entity.setName("fiction");
		entity.setImage("fiction.jpg");
		NumBooksMongo numBooksMongo = new NumBooksMongo();
		numBooksMongo.setTotal(100);
		Map<String, Integer> languages = new HashMap<>();
		languages.put("en", 50);
		languages.put("es", 50);
		numBooksMongo.setLanguages(languages);
		entity.setNumBooks(numBooksMongo);

		// When
		Tag domain = tagMongoMapper.entity2Domain(entity);

		// Then
		assertEquals(entity.getId(), domain.getId());
		assertEquals(entity.getName(), domain.getName());
		assertEquals(entity.getImage(), domain.getImage());
		assertEquals(entity.getNumBooks().getTotal(), domain.getNumBooks().getTotal());
		assertEquals(entity.getNumBooks().getLanguages().get("en"), domain.getNumBooks().getLanguages().get("en"));
		assertEquals(entity.getNumBooks().getLanguages().get("es"), domain.getNumBooks().getLanguages().get("es"));
	}

	@Test
	public void testDomains2Entities_NullDomains_ReturnsNull() {
		// Given
		List<Tag> domains = null;

		// When
		List<TagMongoEntity> entities = tagMongoMapper.domains2Entities(domains);

		// Then
		assertNull(entities);
	}

	@Test
	public void testDomains2Entities_NonNullDomains_ReturnsListOfMappedEntities() {
		// Given
		List<Tag> domains = new ArrayList<>();
		Tag domain1 = new Tag();
		domain1.setId("1");
		Tag domain2 = new Tag();
		domain2.setId("2");
		domains.add(domain1);
		domains.add(domain2);

		// When
		List<TagMongoEntity> entities = tagMongoMapper.domains2Entities(domains);

		// Then
		assertEquals(domains.size(), entities.size());
		for (int i = 0; i < domains.size(); i++) {
			assertEquals(domains.get(i).getId(), entities.get(i).getId());
		}
	}

	@Test
	public void testEntities2Domains_NullEntities_ReturnsNull() {
		// Given
		List<TagMongoEntity> entities = null;

		// When
		List<Tag> domains = tagMongoMapper.entities2Domains(entities);

		// Then
		assertNull(domains);
	}

	@Test
	public void testEntities2Domains_NonNullEntities_ReturnsListOfMappedDomains() {
		// Given
		List<TagMongoEntity> entities = new ArrayList<>();
		TagMongoEntity entity1 = new TagMongoEntity();
		entity1.setId("1");
		TagMongoEntity entity2 = new TagMongoEntity();
		entity2.setId("2");
		entities.add(entity1);
		entities.add(entity2);

		// When
		List<Tag> domains = tagMongoMapper.entities2Domains(entities);

		// Then
		assertEquals(entities.size(), domains.size());
		for (int i = 0; i < entities.size(); i++) {
			assertEquals(entities.get(i).getId(), domains.get(i).getId());
		}
	}
}