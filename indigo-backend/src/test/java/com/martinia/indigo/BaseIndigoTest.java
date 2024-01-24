package com.martinia.indigo;

import com.martinia.indigo.adapters.out.sqlite.repository.BookSqliteRepository;
import com.martinia.indigo.adapters.out.sqlite.service.CalibreRepository;
import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.repositories.ViewRepository;
import com.martinia.indigo.config.BeanDefinitionConfigTest;
import com.martinia.indigo.config.PersistenceConfigTest;
import com.martinia.indigo.configuration.domain.ports.repositories.ConfigurationRepository;
import com.martinia.indigo.notification.domain.ports.repositories.NotificationRepository;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.user.domain.ports.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
//@ContextConfiguration(classes = BaseIndigoTest.TestConfig.class)
@Import({ BeanDefinitionConfigTest.class, PersistenceConfigTest.class })
public class BaseIndigoTest {

	@Resource
	private ApplicationEventPublisher applicationEventPublisher;

	@Resource
	protected ConfigurationRepository configurationRepository;

	@Resource
	protected BookRepository bookRepository;

	@Resource
	protected BookSqliteRepository bookSqliteRepository;


	@Resource
	protected AuthorRepository authorRepository;

	@Resource
	protected NotificationRepository notificationRepository;

	@Resource
	protected TagRepository tagRepository;

	@Resource
	protected UserRepository userRepository;

	@Resource
	protected ViewRepository viewRepository;

	@AfterEach
	public void baseAfterEach() {
		deleteCollections();
		clearInvocations(applicationEventPublisher);
		reset(applicationEventPublisher);
	}

	protected void assertEventPublished(Class event) {
		verify(applicationEventPublisher, times(1)).publishEvent(any(event));
	}

	protected void assertRecursively(final Object actualObject, final Object expectedObject, final String... ignoredFields) {
		assertThat(actualObject).usingRecursiveComparison()
				.withComparatorForType(Comparator.nullsFirst(Comparator.comparing(ZonedDateTime::toInstant)), ZonedDateTime.class)
				.withComparatorForType(Comparator.nullsFirst(Comparator.comparing(Function.identity())), BigDecimal.class)
				.ignoringFieldsMatchingRegexes(ignoredFields)
				.isEqualTo(expectedObject);
	}

	private void deleteCollections(){
		configurationRepository.deleteAll();
		bookRepository.deleteAll();
		authorRepository.deleteAll();
		notificationRepository.deleteAll();
		tagRepository.deleteAll();
		userRepository.deleteAll();
		viewRepository.deleteAll();
	}


}
