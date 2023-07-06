package com.martinia.indigo.tag.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.tag.domain.repository.TagRepository;
import com.martinia.indigo.tag.domain.service.SetTagImageUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

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
		verify(tagRepository).setImage(sourceTag, image);
	}
}
