package com.martinia.indigo.metadata.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.domain.model.MetadataProcessEnum;
import com.martinia.indigo.metadata.domain.model.MetadataProcessType;
import com.martinia.indigo.metadata.domain.model.commands.StartFillAuthorsMetadataCommand;
import com.martinia.indigo.metadata.domain.model.commands.StartFillBooksMetadataCommand;
import com.martinia.indigo.metadata.domain.model.commands.StartFillReviewsMetadataCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StartMetadataUseCaseImplTest extends BaseIndigoTest {

	@MockBean
	private MetadataSingleton metadataSingleton;

	@MockBean
	private CommandBus commandBus;

	@Resource
	private StartMetadataUseCaseImpl startMetadataUseCase;

	@BeforeEach
	public void setUp() {
		when(metadataSingleton.isRunning()).thenReturn(true);
	}

	private static Stream<Arguments> provideStartMetadataParameters() {
		return Stream.of(Arguments.of(MetadataProcessType.FULL, MetadataProcessEnum.LOAD),
				Arguments.of(MetadataProcessType.FULL, MetadataProcessEnum.BOOKS),
				Arguments.of(MetadataProcessType.FULL, MetadataProcessEnum.AUTHORS),
				Arguments.of(MetadataProcessType.FULL, MetadataProcessEnum.REVIEWS),
				Arguments.of(MetadataProcessType.PARTIAL, MetadataProcessEnum.LOAD),
				Arguments.of(MetadataProcessType.PARTIAL, MetadataProcessEnum.BOOKS),
				Arguments.of(MetadataProcessType.PARTIAL, MetadataProcessEnum.AUTHORS),
				Arguments.of(MetadataProcessType.PARTIAL, MetadataProcessEnum.REVIEWS));
	}

	@ParameterizedTest
	@MethodSource("provideStartMetadataParameters")
	public void testStart_MetadataProcess(MetadataProcessType type, MetadataProcessEnum entity) {
		// Given
		String lang = "en";

		// When
		startMetadataUseCase.start(lang, type.name(), entity.name());

		// Then
		verify(metadataSingleton, times(1)).stop();
		verify(metadataSingleton, times(1)).start(type.name(), entity.name());
		if (type == MetadataProcessType.FULL) {

			if (entity == MetadataProcessEnum.BOOKS) {
				verify(commandBus, times(1)).execute(any(StartFillBooksMetadataCommand.class));
			}
			else {
				if (entity == MetadataProcessEnum.AUTHORS) {
					verify(commandBus, times(1)).execute(any(StartFillAuthorsMetadataCommand.class));
				}
				else {
					if (entity == MetadataProcessEnum.REVIEWS) {
						verify(commandBus, times(1)).execute(any(StartFillReviewsMetadataCommand.class));
					}
				}
			}

		}
		else {

			if (entity == MetadataProcessEnum.BOOKS) {
				verify(commandBus, times(1)).execute(any(StartFillBooksMetadataCommand.class));
			}
			else {
				if (entity == MetadataProcessEnum.AUTHORS) {
					verify(commandBus, times(1)).execute(any(StartFillAuthorsMetadataCommand.class));
				}
				else {
					if (entity == MetadataProcessEnum.REVIEWS) {
						verify(commandBus, times(1)).execute(any(StartFillReviewsMetadataCommand.class));
					}
				}
			}

		}
	}

}

