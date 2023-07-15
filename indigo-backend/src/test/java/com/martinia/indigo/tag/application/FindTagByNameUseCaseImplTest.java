package com.martinia.indigo.tag.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.domain.ports.usecases.FindTagByNameUseCase;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import com.martinia.indigo.tag.infrastructure.mongo.mappers.TagMongoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class FindTagByNameUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindTagByNameUseCase findTagByNameUseCase;

	@MockBean
	private TagRepository tagRepository;

	@MockBean
	private TagMongoMapper tagMongoMapper;

	@Test
	public void testFindByName_WithExistingTag_ReturnsTag() {
		// Given
		String tagName = "Tag 1";

		TagMongoEntity tagEntity = new TagMongoEntity();
		tagEntity.setId("1");
		tagEntity.setName(tagName);

		Tag tag = new Tag();
		tag.setId("1");
		tag.setName(tagName);

		when(tagRepository.findByName(tagName)).thenReturn(Optional.of(tagEntity));
		when(tagMongoMapper.entity2Domain(tagEntity)).thenReturn(tag);

		// When
		Optional<Tag> result = findTagByNameUseCase.findByName(tagName);

		// Then
		assertEquals(Optional.of(tag), result);
	}

	@Test
	public void testFindByName_WithNonExistingTag_ReturnsEmptyOptional() {
		// Given
		String tagName = "Non-existing Tag";

		when(tagRepository.findByName(tagName)).thenReturn(Optional.empty());

		// When
		Optional<Tag> result = findTagByNameUseCase.findByName(tagName);

		// Then
		assertEquals(Optional.empty(), result);
	}
}

