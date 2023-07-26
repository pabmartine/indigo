package com.martinia.indigo.config;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.repositories.ViewRepository;
import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationRepository;
import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;

public class TestContainersTest extends BaseIndigoTest {
	@Resource
	private ConfigurationRepository configurationRepository;

	@Resource
	private BookRepository bookRepository;

	@Resource
	private AuthorRepository authorRepository;

	@Resource
	private NotificationRepository notificationRepository;

	@Resource
	private TagRepository tagRepository;

	@Resource
	private UserRepository userRepository;

	@Resource
	private ViewRepository viewRepository;

	@Test
	void testConfigurations() {
		long count = configurationRepository.count();
		assertEquals(0, count);

		configurationRepository.save(ConfigurationMongoEntity.builder().key("key").value("value").build());
		count = configurationRepository.count();
		assertEquals(1, count);
	}

	@Test
	void testBooks() {
		long count = bookRepository.count();
		assertEquals(0, count);
	}

	@Test
	void testAuthors() {
		long count = authorRepository.count();
		assertEquals(0, count);
	}

	@Test
	void testNotifications() {
		long count = notificationRepository.count();
		assertEquals(0, count);
	}

	@Test
	void testTags() {
		long count = tagRepository.count();
		assertEquals(0, count);
	}

	@Test
	void testUser() {
		long count = userRepository.count();
		assertEquals(0, count);
	}

	@Test
	void testViews() {
		long count = viewRepository.count();
		assertEquals(0, count);
	}
}
