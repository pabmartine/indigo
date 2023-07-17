package com.martinia.indigo;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
//@ActiveProfiles("test")
//@Import({ PersistenceConfigTest.class })
public class BaseIndigoTest {

//	@MockBean
//	private DataSource dataSource;

	protected void assertRecursively(final Object actualObject, final Object expectedObject, final String... ignoredFields) {
		assertThat(actualObject).usingRecursiveComparison()
				.withComparatorForType(Comparator.nullsFirst(Comparator.comparing(ZonedDateTime::toInstant)), ZonedDateTime.class)
				.withComparatorForType(Comparator.nullsFirst(Comparator.comparing(Function.identity())), BigDecimal.class)
				.ignoringFieldsMatchingRegexes(ignoredFields)
				.isEqualTo(expectedObject);
	}

}
