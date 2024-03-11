package com.martinia.indigo.tag.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.domain.ports.usecases.FindAllTagsUseCase;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import com.martinia.indigo.tag.infrastructure.mongo.mappers.TagMongoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class FindAllTagsUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindAllTagsUseCase findAllTagsUseCase;

	@MockBean
	private TagRepository tagRepository;

	@MockBean
	private TagMongoMapper tagMongoMapper;

	@Test
	public void testFindAll_ReturnsAllTags() {
		// Given
		List<String> languages = Arrays.asList("English", "Spanish");
		String sort = "name";
		String order = "asc";

		final Map<String, Integer> langs = new HashMap<>();
		langs.put("eng", 1);

		TagMongoEntity tag1 = new TagMongoEntity();
		tag1.setId("1");
		tag1.setName("Tag 1");
		tag1.setNumBooks(NumBooksMongo.builder().languages(langs).total(1).build());

		TagMongoEntity tag2 = new TagMongoEntity();
		tag2.setId("2");
		tag2.setName("Tag 2");
		tag2.setNumBooks(NumBooksMongo.builder().languages(langs).total(1).build());


		List<TagMongoEntity> tagEntities = Arrays.asList(tag1, tag2);

		when(tagRepository.findAll(languages, Sort.by(Sort.Direction.fromString(order), sort))).thenReturn(tagEntities);
		when(tagMongoMapper.entities2Domains(tagEntities)).thenReturn(Arrays.asList(new Tag(), new Tag()));

		// When
		List<Tag> tags = findAllTagsUseCase.findAll(languages, sort, order);

		// Then
		assertEquals(2, tags.size());
		// Assert other expectations
	}
}

