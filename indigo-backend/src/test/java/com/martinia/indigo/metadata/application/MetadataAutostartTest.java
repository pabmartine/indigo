package com.martinia.indigo.metadata.application;

import com.martinia.indigo.metadata.domain.ports.usecases.StartMetadataUseCase;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MetadataAutostartTest {

	private final StartMetadataUseCase useCase = mock(StartMetadataUseCase.class);
	private final ApplicationContextRunner runner = new ApplicationContextRunner()
			.withUserConfiguration(MetadataAutostart.class)
			.withBean(StartMetadataUseCase.class, () -> useCase);

	@Test
	void disabledByDefault() {
		runner.run(context -> {
			assertThat(context).hasNotFailed();
			context.publishEvent(new ApplicationReadyEvent(new SpringApplication(), new String[0],
					context.getSourceApplicationContext(), Duration.ZERO));
			verifyNoInteractions(useCase);
		});
	}

	@ParameterizedTest
	@CsvSource({ "AUTHORS_ALL,FULL,AUTHORS", "AUTHORS_EMPTY,PARTIAL,AUTHORS",
			"BOOKS_ALL,FULL,BOOKS", "BOOKS_EMPTY,PARTIAL,BOOKS",
			"REVIEWS_ALL,FULL,REVIEWS", "REVIEWS_EMPTY,PARTIAL,REVIEWS" })
	void startsSelectedProcessOnlyWhenReady(String type, String processType, String entity) {
		runner.withPropertyValues("metadata.autostart.enabled=true", "metadata.autostart.type=" + type,
				"metadata.autostart.lang=en").run(context -> {
			assertThat(context).hasNotFailed();
			verifyNoInteractions(useCase);
			context.publishEvent(new ApplicationReadyEvent(new SpringApplication(), new String[0],
					context.getSourceApplicationContext(), Duration.ZERO));
			verify(useCase).start("en", processType, entity);
			verifyNoMoreInteractions(useCase);
		});
	}

	@Test
	void bindsEnvironmentVariables() {
		runner.withInitializer(context -> context.getEnvironment().getPropertySources().addFirst(
				new SystemEnvironmentPropertySource("autostart-test", Map.of(
						"METADATA_AUTOSTART_ENABLED", "true", "METADATA_AUTOSTART_TYPE", "BOOKS_EMPTY",
						"METADATA_AUTOSTART_LANG", "fr"))))
				.run(context -> {
					assertThat(context).hasNotFailed();
					context.getBean(MetadataAutostart.class).start();
					verify(useCase).start("fr", "PARTIAL", "BOOKS");
				});
	}

	@Test
	void enabledUsesDefaultTypeAndLanguage() {
		runner.withPropertyValues("metadata.autostart.enabled=true").run(context -> {
			context.getBean(MetadataAutostart.class).start();
			verify(useCase).start("es", "PARTIAL", "AUTHORS");
		});
	}

	@Test
	void rejectsUnknownType() {
		runner.withPropertyValues("metadata.autostart.type=UNKNOWN").run(context -> {
			assertThat(context).hasFailed();
			verifyNoInteractions(useCase);
		});
	}

	@Test
	void launchFailureDoesNotFailApplicationStartup() {
		doThrow(new IllegalStateException("Already running")).when(useCase).start("es", "PARTIAL", "AUTHORS");
		runner.withPropertyValues("metadata.autostart.enabled=true").run(context -> {
			context.publishEvent(new ApplicationReadyEvent(new SpringApplication(), new String[0],
					context.getSourceApplicationContext(), Duration.ZERO));
			assertThat(context).hasNotFailed();
			verify(useCase).start("es", "PARTIAL", "AUTHORS");
		});
	}
}
