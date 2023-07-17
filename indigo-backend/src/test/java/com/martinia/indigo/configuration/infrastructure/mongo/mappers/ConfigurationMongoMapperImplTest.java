package com.martinia.indigo.configuration.infrastructure.mongo.mappers;

import com.martinia.indigo.configuration.domain.model.Configuration;
import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationMongoMapperImplTest {

	@Test
	public void testDomain2Entity_withValidDomain_shouldReturnEntity() {
		// Arrange
		Configuration domain = new Configuration();
		domain.setId("1");
		domain.setKey("key");
		domain.setValue("value");

		ConfigurationMongoMapperImpl mapper = new ConfigurationMongoMapperImpl();

		// Act
		ConfigurationMongoEntity entity = mapper.domain2Entity(domain);

		// Assert
		Assertions.assertNotNull(entity);
		Assertions.assertEquals("1", entity.getId());
		Assertions.assertEquals("key", entity.getKey());
		Assertions.assertEquals("value", entity.getValue());
	}

	@Test
	public void testDomain2Entity_withNullDomain_shouldReturnNull() {
		// Arrange
		Configuration domain = null;

		ConfigurationMongoMapperImpl mapper = new ConfigurationMongoMapperImpl();

		// Act
		ConfigurationMongoEntity entity = mapper.domain2Entity(domain);

		// Assert
		Assertions.assertNull(entity);
	}

	@Test
	public void testEntity2Domain_withValidEntity_shouldReturnDomain() {
		// Arrange
		ConfigurationMongoEntity entity = new ConfigurationMongoEntity();
		entity.setId("1");
		entity.setKey("key");
		entity.setValue("value");

		ConfigurationMongoMapperImpl mapper = new ConfigurationMongoMapperImpl();

		// Act
		Configuration domain = mapper.entity2Domain(entity);

		// Assert
		Assertions.assertNotNull(domain);
		Assertions.assertEquals("1", domain.getId());
		Assertions.assertEquals("key", domain.getKey());
		Assertions.assertEquals("value", domain.getValue());
	}

	@Test
	public void testEntity2Domain_withNullEntity_shouldReturnNull() {
		// Arrange
		ConfigurationMongoEntity entity = null;

		ConfigurationMongoMapperImpl mapper = new ConfigurationMongoMapperImpl();

		// Act
		Configuration domain = mapper.entity2Domain(entity);

		// Assert
		Assertions.assertNull(domain);
	}

	@Test
	public void testDomainList2EntityList_withValidDomainList_shouldReturnEntityList() {
		// Arrange
		List<Configuration> domainList = new ArrayList<>();
		Configuration domain1 = new Configuration();
		domain1.setId("1");
		domain1.setKey("key1");
		domain1.setValue("value1");
		Configuration domain2 = new Configuration();
		domain2.setId("2");
		domain2.setKey("key2");
		domain2.setValue("value2");
		domainList.add(domain1);
		domainList.add(domain2);

		ConfigurationMongoMapperImpl mapper = new ConfigurationMongoMapperImpl();

		// Act
		List<ConfigurationMongoEntity> entityList = mapper.domain2Entity(domainList);

		// Assert
		Assertions.assertNotNull(entityList);
		Assertions.assertEquals(2, entityList.size());
		Assertions.assertEquals("1", entityList.get(0).getId());
		Assertions.assertEquals("key1", entityList.get(0).getKey());
		Assertions.assertEquals("value1", entityList.get(0).getValue());
		Assertions.assertEquals("2", entityList.get(1).getId());
		Assertions.assertEquals("key2", entityList.get(1).getKey());
		Assertions.assertEquals("value2", entityList.get(1).getValue());
	}

	@Test
	public void testDomainList2EntityList_withNullDomainList_shouldReturnNull() {
		// Arrange
		List<Configuration> domainList = null;

		ConfigurationMongoMapperImpl mapper = new ConfigurationMongoMapperImpl();

		// Act
		List<ConfigurationMongoEntity> entityList = mapper.domain2Entity(domainList);

		// Assert
		Assertions.assertNull(entityList);
	}

	@Test
	public void testEntityList2DomainList_withValidEntityList_shouldReturnDomainList() {
		// Arrange
		List<ConfigurationMongoEntity> entityList = new ArrayList<>();
		ConfigurationMongoEntity entity1 = new ConfigurationMongoEntity();
		entity1.setId("1");
		entity1.setKey("key1");
		entity1.setValue("value1");
		ConfigurationMongoEntity entity2 = new ConfigurationMongoEntity();
		entity2.setId("2");
		entity2.setKey("key2");
		entity2.setValue("value2");
		entityList.add(entity1);
		entityList.add(entity2);

		ConfigurationMongoMapperImpl mapper = new ConfigurationMongoMapperImpl();

		// Act
		List<Configuration> domainList = mapper.entity2Domain(entityList);

		// Assert
		Assertions.assertNotNull(domainList);
		Assertions.assertEquals(2, domainList.size());
		Assertions.assertEquals("1", domainList.get(0).getId());
		Assertions.assertEquals("key1", domainList.get(0).getKey());
		Assertions.assertEquals("value1", domainList.get(0).getValue());
		Assertions.assertEquals("2", domainList.get(1).getId());
		Assertions.assertEquals("key2", domainList.get(1).getKey());
		Assertions.assertEquals("value2", domainList.get(1).getValue());
	}

	@Test
	public void testEntityList2DomainList_withNullEntityList_shouldReturnNull() {
		// Arrange
		List<ConfigurationMongoEntity> entityList = null;

		ConfigurationMongoMapperImpl mapper = new ConfigurationMongoMapperImpl();

		// Act
		List<Configuration> domainList = mapper.entity2Domain(entityList);

		// Assert
		Assertions.assertNull(domainList);
	}
}
