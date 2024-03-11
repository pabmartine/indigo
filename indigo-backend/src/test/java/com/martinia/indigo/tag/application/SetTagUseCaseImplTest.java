package com.martinia.indigo.tag.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.domain.ports.usecases.SetTagImageUseCase;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SetTagUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private SetTagImageUseCase setTagImageUseCase;

	@MockBean
	private TagRepository tagRepository;

	@Test
	public void testSetTagImage_TagExists_SetsImage() {
		// Given
		String sourceTagId = "1";
		String image = "https://example.com/image.jpg";

		TagMongoEntity tagEntity = new TagMongoEntity();
		tagEntity.setId(sourceTagId);

		when(tagRepository.findById(sourceTagId)).thenReturn(Optional.of(tagEntity));

		// When
		setTagImageUseCase.setImage(sourceTagId, image);

		// Then
		verify(tagRepository).save(tagEntity);
	}

	@Test
	public void testSetTagImage_TagDoesNotExist_DoesNothing() {
		// Given
		String sourceTagId = "1";
		String image = "https://example.com/image.jpg";

		when(tagRepository.findById(sourceTagId)).thenReturn(Optional.empty());

		// When
		setTagImageUseCase.setImage(sourceTagId, image);

		// Then
		verify(tagRepository, Mockito.never()).save(Mockito.any());
	}
}
