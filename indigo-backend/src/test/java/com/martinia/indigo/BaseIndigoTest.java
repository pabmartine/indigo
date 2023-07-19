package com.martinia.indigo;

import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.config.BeanDefinitionConfigTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;

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
//@ActiveProfiles("test")
@Import({ BeanDefinitionConfigTest.class })
public class BaseIndigoTest {

	//	@MockBean
	//	private DataSource dataSource;

	@Resource
	private ApplicationEventPublisher applicationEventPublisher;

//	@SpyBean
//	protected EventBus eventBus;
//
//	@SpyBean
//	protected CommandBus commandBus;

	@AfterEach
	public void baseAfterEach() {
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

}
