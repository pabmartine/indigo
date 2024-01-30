package com.martinia.indigo.tag.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import com.martinia.indigo.tag.infrastructure.mongo.mappers.TagMongoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class UpdateImageTagUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private UpdateImageTagUseCaseImpl useCase;

	@MockBean
	private TagRepository tagRepository;

	@MockBean
	private CommandBus commandBus;

	@MockBean
	private TagMongoMapper tagMongoMapper;

	@Test
	public void testUpdateImageWithExistingSource() {
		// Given
		String source = "valid_source";
		TagMongoEntity entitySource = new TagMongoEntity();
		entitySource.setName("test");
		TagMongoEntity updatedEntitySource = new TagMongoEntity();

		when(tagRepository.findById(any())).thenReturn(Optional.of(entitySource));
		when(commandBus.executeAndWait(any())).thenReturn("url");
		when(tagRepository.save(any())).thenReturn(updatedEntitySource);
		when(tagMongoMapper.entity2Domain(any())).thenReturn(new Tag());

		// When
		Tag result = useCase.updateImage(source);

		// Then
		assertNotNull(result);

		verify(tagRepository).findById(any());
		verify(commandBus).executeAndWait(any());
		verify(tagRepository).save(any());
		verify(tagMongoMapper).entity2Domain(any());
	}

	@Test
	public void testUpdateImageWithNonExistingSource() {
		// Given
		String source = "invalid_source";

		when(tagRepository.findById(source)).thenReturn(Optional.empty());

		// When
		Tag result = useCase.updateImage(source);

		// Then
		assertNull(result);

		verify(tagRepository).findById(source);
		verifyNoInteractions(commandBus);
	}
}