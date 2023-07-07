package com.martinia.indigo.tag.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.common.model.NumBooks;
import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.domain.repository.TagRepository;
import com.martinia.indigo.tag.domain.service.FindTagByNameUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;


public class FindTagByNameUseCaseImplTest extends BaseIndigoTest {

	@MockBean
	private TagRepository tagRepository;

	@Resource
	private FindTagByNameUseCase findTagByNameUseCase;

	@Test
	public void testFindByName_TagExists() {
		// Given
		String tagName = "Java";
		Tag expectedTag = new Tag("1", tagName, "image.png", new NumBooks());

		when(tagRepository.findByName(tagName)).thenReturn(Optional.of(expectedTag));

		// When
		Optional<Tag> result = findTagByNameUseCase.findByName(tagName);

		// Then
		assertTrue(result.isPresent());
		assertEquals(expectedTag, result.get());
	}

	@Test
	public void testFindByName_TagNotExists() {
		// Given
		String tagName = "Python";

		when(tagRepository.findByName(tagName)).thenReturn(Optional.empty());

		// When
		Optional<Tag> result = findTagByNameUseCase.findByName(tagName);

		// Then
		assertTrue(result.isEmpty());
	}
}
