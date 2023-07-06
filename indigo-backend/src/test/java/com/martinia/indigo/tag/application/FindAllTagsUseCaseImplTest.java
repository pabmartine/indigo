package com.martinia.indigo.tag.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.domain.model.inner.NumBooks;
import com.martinia.indigo.tag.domain.model.Tag;
import com.martinia.indigo.tag.domain.repository.TagRepository;
import com.martinia.indigo.tag.domain.service.FindAllTagsUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


public class FindAllTagsUseCaseImplTest extends BaseIndigoTest {

	@MockBean
	private TagRepository tagRepository;

	@Resource
	private FindAllTagsUseCase findAllTagsUseCase;

	@Test
	public void testFindAll() {
		// Given
		List<String> languages = new ArrayList<>();
		languages.add("Java");
		languages.add("Python");
		String sort = "name";
		String order = "asc";

		List<Tag> expectedTags = new ArrayList<>();
		expectedTags.add(new Tag("id", "name", "image", new NumBooks(0, new HashMap<>())));
		expectedTags.add(new Tag("id2", "name2", "image2", new NumBooks(0, new HashMap<>())));

		when(tagRepository.findAll(languages, sort, order)).thenReturn(expectedTags);

		// When
		List<Tag> actualTags = findAllTagsUseCase.findAll(languages, sort, order);

		// Then
		assertEquals(expectedTags, actualTags);
	}
}
