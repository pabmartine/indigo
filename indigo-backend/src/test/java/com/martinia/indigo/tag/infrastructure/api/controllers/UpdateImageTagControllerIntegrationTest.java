package com.martinia.indigo.tag.infrastructure.api.controllers;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.metadata.domain.model.commands.FindImageTagMetadataCommand;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UpdateImageTagControllerIntegrationTest extends BaseIndigoIntegrationTest {

	private static final String BASE_PATH = "/api/tag";

	public static final String ID_1 = "id1";
	public static final String NAME_1 = "name1";
	public static final String IMAGE_1 = "image1";
	public static final int ONE = 1;
	@Resource
	private MockMvc mockMvc;
	@MockBean
	private CommandBus commandBus;

	@Test
	public void updateImageTagNotExist() throws Exception {
		// given
		String source = "sourceTag";

		insertTag();

		// When
		ResultActions result = mockMvc.perform(
				get(BASE_PATH + "/image/update").param("source", source).contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(status().isOk());
		Assert.assertEquals(IMAGE_1, tagRepository.findById(ID_1).get().getImage());

	}

	@Test
	public void updateImageTagOk() throws Exception {
		// given
		String image = "targetImage";

		insertTag();

		when(commandBus.executeAndWait(any(FindImageTagMetadataCommand.class))).thenReturn(image);

		// When
		ResultActions result = mockMvc.perform(
				get(BASE_PATH + "/image/update").param("source", ID_1).contentType(MediaType.APPLICATION_JSON));

		//Then
		result.andExpect(status().isOk());
		Assert.assertEquals(image, tagRepository.findById(ID_1).get().getImage());

	}

	private void insertTag() {
		final Map<String, Integer> languages = new HashMap<>();
		languages.put("es", ONE);
		final TagMongoEntity tagEntity = TagMongoEntity.builder()
				.id(ID_1)
				.image(IMAGE_1)
				.name(NAME_1)
				.numBooks(NumBooksMongo.builder().languages(languages).total(ONE).build())
				.build();
		tagRepository.save(tagEntity);
	}
}