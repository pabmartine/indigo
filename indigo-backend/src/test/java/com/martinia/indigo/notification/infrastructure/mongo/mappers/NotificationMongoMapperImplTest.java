package com.martinia.indigo.notification.infrastructure.mongo.mappers;

import com.martinia.indigo.notification.domain.model.Notification;
import com.martinia.indigo.notification.infrastructure.mongo.entities.NotificationMongoEntity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NotificationMongoMapperImplTest {

	private final NotificationMongoMapper notificationMongoMapper = new NotificationMongoMapperImpl();

	@Test
	public void testDomain2Entity_NullDomain_ReturnsNull() {
		// Given
		Notification domain = null;

		// When
		NotificationMongoEntity entity = notificationMongoMapper.domain2Entity(domain);

		// Then
		assertNull(entity);
	}

	@Test
	public void testDomain2Entity_NonNullDomain_ReturnsEntityWithMappedValues() {
		// Given
		Notification domain = new Notification();
		domain.setId("1");
		domain.setType("KINDLE");
		domain.setUser("user123");
		domain.setBook("book123");
		domain.setStatus("PENDING");
		domain.setError("Error message");
		domain.setReadUser(true);
		domain.setReadAdmin(false);
		Date sendDate = new Date();
		domain.setSendDate(sendDate);

		// When
		NotificationMongoEntity entity = notificationMongoMapper.domain2Entity(domain);

		// Then
		assertEquals(domain.getId(), entity.getId());
		assertEquals(domain.getType(), entity.getType());
		assertEquals(domain.getUser(), entity.getUser());
		assertEquals(domain.getBook(), entity.getBook());
		assertEquals(domain.getStatus(), entity.getStatus());
		assertEquals(domain.getError(), entity.getError());
		assertEquals(domain.isReadUser(), entity.isReadUser());
		assertEquals(domain.isReadAdmin(), entity.isReadAdmin());
		assertEquals(domain.getSendDate(), entity.getSendDate());
	}

	@Test
	public void testEntity2Domain_NullEntity_ReturnsNull() {
		// Given
		NotificationMongoEntity entity = null;

		// When
		Notification domain = notificationMongoMapper.entity2Domain(entity);

		// Then
		assertNull(domain);
	}

	@Test
	public void testEntity2Domain_NonNullEntity_ReturnsDomainWithMappedValues() {
		// Given
		NotificationMongoEntity entity = new NotificationMongoEntity();
		entity.setId("1");
		entity.setType("KINDLE");
		entity.setUser("user123");
		entity.setBook("book123");
		entity.setStatus("PENDING");
		entity.setError("Error message");
		entity.setReadUser(true);
		entity.setReadAdmin(false);
		Date sendDate = new Date();
		entity.setSendDate(sendDate);

		// When
		Notification domain = notificationMongoMapper.entity2Domain(entity);

		// Then
		assertEquals(entity.getId(), domain.getId());
		assertEquals(entity.getType(), domain.getType());
		assertEquals(entity.getUser(), domain.getUser());
		assertEquals(entity.getBook(), domain.getBook());
		assertEquals(entity.getStatus(), domain.getStatus());
		assertEquals(entity.getError(), domain.getError());
		assertEquals(entity.isReadUser(), domain.isReadUser());
		assertEquals(entity.isReadAdmin(), domain.isReadAdmin());
		assertEquals(entity.getSendDate(), domain.getSendDate());
	}

	@Test
	public void testDomains2Entities_NullDomains_ReturnsNull() {
		// Given
		List<Notification> domains = null;

		// When
		List<NotificationMongoEntity> entities = notificationMongoMapper.domains2Entities(domains);

		// Then
		assertNull(entities);
	}

	@Test
	public void testDomains2Entities_NonNullDomains_ReturnsListOfMappedEntities() {
		// Given
		List<Notification> domains = new ArrayList<>();
		Notification domain1 = new Notification();
		domain1.setId("1");
		domain1.setType("KINDLE");
		domain1.setUser("user123");
		domain1.setBook("book123");
		domain1.setStatus("PENDING");
		domain1.setError("Error message");
		domain1.setReadUser(true);
		domain1.setReadAdmin(false);
		Date sendDate1 = new Date();
		domain1.setSendDate(sendDate1);
		Notification domain2 = new Notification();
		domain2.setId("2");
		domain2.setType("EMAIL");
		domain2.setUser("user456");
		domain2.setBook("book456");
		domain2.setStatus("SUCCESS");
		domain2.setError(null);
		domain2.setReadUser(false);
		domain2.setReadAdmin(true);
		Date sendDate2 = new Date();
		domain2.setSendDate(sendDate2);
		domains.add(domain1);
		domains.add(domain2);

		// When
		List<NotificationMongoEntity> entities = notificationMongoMapper.domains2Entities(domains);

		// Then
		assertEquals(domains.size(), entities.size());
		for (int i = 0; i < domains.size(); i++) {
			assertEquals(domains.get(i).getId(), entities.get(i).getId());
			assertEquals(domains.get(i).getType(), entities.get(i).getType());
			assertEquals(domains.get(i).getUser(), entities.get(i).getUser());
			assertEquals(domains.get(i).getBook(), entities.get(i).getBook());
			assertEquals(domains.get(i).getStatus(), entities.get(i).getStatus());
			assertEquals(domains.get(i).getError(), entities.get(i).getError());
			assertEquals(domains.get(i).isReadUser(), entities.get(i).isReadUser());
			assertEquals(domains.get(i).isReadAdmin(), entities.get(i).isReadAdmin());
			assertEquals(domains.get(i).getSendDate(), entities.get(i).getSendDate());
		}
	}

	@Test
	public void testEntities2Domains_NullEntities_ReturnsNull() {
		// Given
		List<NotificationMongoEntity> entities = null;

		// When
		List<Notification> domains = notificationMongoMapper.entities2Domains(entities);

		// Then
		assertNull(domains);
	}

	@Test
	public void testEntities2Domains_NonNullEntities_ReturnsListOfMappedDomains() {
		// Given
		List<NotificationMongoEntity> entities = new ArrayList<>();
		NotificationMongoEntity entity1 = new NotificationMongoEntity();
		entity1.setId("1");
		entity1.setType("KINDLE");
		entity1.setUser("user123");
		entity1.setBook("book123");
		entity1.setStatus("PENDING");
		entity1.setError("Error message");
		entity1.setReadUser(true);
		entity1.setReadAdmin(false);
		Date sendDate1 = new Date();
		entity1.setSendDate(sendDate1);
		NotificationMongoEntity entity2 = new NotificationMongoEntity();
		entity2.setId("2");
		entity2.setType("EMAIL");
		entity2.setUser("user456");
		entity2.setBook("book456");
		entity2.setStatus("SUCCESS");
		entity2.setError(null);
		entity2.setReadUser(false);
		entity2.setReadAdmin(true);
		Date sendDate2 = new Date();
		entity2.setSendDate(sendDate2);
		entities.add(entity1);
		entities.add(entity2);

		// When
		List<Notification> domains = notificationMongoMapper.entities2Domains(entities);

		// Then
		assertEquals(entities.size(), domains.size());
		for (int i = 0; i < entities.size(); i++) {
			assertEquals(entities.get(i).getId(), domains.get(i).getId());
			assertEquals(entities.get(i).getType(), domains.get(i).getType());
			assertEquals(entities.get(i).getUser(), domains.get(i).getUser());
			assertEquals(entities.get(i).getBook(), domains.get(i).getBook());
			assertEquals(entities.get(i).getStatus(), domains.get(i).getStatus());
			assertEquals(entities.get(i).getError(), domains.get(i).getError());
			assertEquals(entities.get(i).isReadUser(), domains.get(i).isReadUser());
			assertEquals(entities.get(i).isReadAdmin(), domains.get(i).isReadAdmin());
			assertEquals(entities.get(i).getSendDate(), domains.get(i).getSendDate());
		}
	}
}
