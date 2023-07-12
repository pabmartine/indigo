package com.martinia.indigo.tag.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.domain.ports.usecases.SetTagImageUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;


public class SetTagUseCaseImplTest extends BaseIndigoTest {

	@MockBean
	private TagRepository tagRepository;

	@Resource
	private SetTagImageUseCase setTagUseCase;

	@Test
	public void testSetImage() {
		// Given
		String sourceTag = "SourceTag";
		String image = "image.jpg";

		// When
		setTagUseCase.setImage(sourceTag, image);

		// Then
		verify(tagRepository).save(any());
	}
}
