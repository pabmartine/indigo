package com.martinia.indigo.metadata.infrastructure.api.controllers;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.metadata.domain.model.MetadataProcessEnum;
import com.martinia.indigo.metadata.domain.model.MetadataProcessType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.stream.Stream;

public class StartMetadataControllerIntegrationTest extends BaseIndigoIntegrationTest {

	@MockBean
	protected CommandBus commandBus;

    @MockBean
    private com.martinia.indigo.metadata.application.reviews.ReviewQueueService reviewQueue;

	@ParameterizedTest(name = "{index} -> {0} ")
	@MethodSource("entities")
	@WithMockUser(authorities = "ADMIN")
	public void testStartMetadata(final MetadataProcessType type, final MetadataProcessEnum entity) throws Exception {
		// Given
		String lang = "en";

		// When
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/metadata/start")
				.param("lang", lang)
				.param("type", type.name())
				.param("entity", entity.name())
				.contentType(MediaType.APPLICATION_JSON));

		// Then
		result.andExpect(MockMvcResultMatchers.status().isOk());
        if (entity == MetadataProcessEnum.REVIEWS) {
            Mockito.verify(reviewQueue).start(type == MetadataProcessType.FULL, lang, false);
            Mockito.verifyNoInteractions(commandBus);
        } else {
            Mockito.verify(commandBus).execute(Mockito.any());
            Mockito.verifyNoInteractions(reviewQueue);
        }
	}

	static Stream<Arguments> entities() {
		return Stream.of(
//				Arguments.of(MetadataProcessType.FULL, MetadataProcessEnum.LOAD),
				Arguments.of(MetadataProcessType.FULL, MetadataProcessEnum.BOOKS),
				Arguments.of(MetadataProcessType.FULL, MetadataProcessEnum.AUTHORS),
				Arguments.of(MetadataProcessType.FULL, MetadataProcessEnum.REVIEWS),
//				Arguments.of(MetadataProcessType.PARTIAL, MetadataProcessEnum.LOAD),
				Arguments.of(MetadataProcessType.PARTIAL, MetadataProcessEnum.BOOKS),
				Arguments.of(MetadataProcessType.PARTIAL, MetadataProcessEnum.AUTHORS),
				Arguments.of(MetadataProcessType.PARTIAL, MetadataProcessEnum.REVIEWS));
	}
}
