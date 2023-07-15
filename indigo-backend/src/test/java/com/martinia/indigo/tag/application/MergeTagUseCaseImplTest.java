package com.martinia.indigo.tag.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.domain.ports.usecases.MergeTagUseCase;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MergeTagUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private MergeTagUseCase mergeTagUseCase;

	@MockBean
	private TagRepository tagRepository;

	@MockBean
	private BookRepository bookRepository;

	@Test
	public void testMerge_TagsExist_MergesTags() {
		// Given
		String sourceTagId = "1";
		String targetTagId = "2";

		final Map<String, Integer> langs = new HashMap<>();
		langs.put("eng", 1);

		TagMongoEntity sourceTagEntity = new TagMongoEntity();
		sourceTagEntity.setId(sourceTagId);
		sourceTagEntity.setName("Source Tag");
		sourceTagEntity.setNumBooks(NumBooksMongo.builder().languages(langs).total(1).build());

		TagMongoEntity targetTagEntity = new TagMongoEntity();
		targetTagEntity.setId(targetTagId);
		targetTagEntity.setName("Target Tag");
		targetTagEntity.setNumBooks(NumBooksMongo.builder().languages(langs).total(1).build());


		List<BookMongoEntity> books = new ArrayList<>();
		BookMongoEntity book1 = new BookMongoEntity();
		book1.setId("1");
		final List<String> tags = new ArrayList<>();
		tags.add("tag1");
		book1.setTags(tags);
		books.add(book1);
		BookMongoEntity book2 = new BookMongoEntity();
		book2.setId("2");
		book2.setTags(tags);
		books.add(book2);

		when(tagRepository.findById(sourceTagId)).thenReturn(Optional.of(sourceTagEntity));
		when(tagRepository.findById(targetTagId)).thenReturn(Optional.of(targetTagEntity));
		when(bookRepository.findByTag("Source Tag")).thenReturn(books);

		// When
		mergeTagUseCase.merge(sourceTagId, targetTagId);

		// Then
		verify(bookRepository).saveAll(books);
		verify(tagRepository).save(targetTagEntity);
		verify(tagRepository).delete(sourceTagEntity);
	}

	@Test
	public void testMerge_TagsDoNotExist_DoesNothing() {
		// Given
		String sourceTagId = "1";
		String targetTagId = "2";

		when(tagRepository.findById(sourceTagId)).thenReturn(Optional.empty());
		when(tagRepository.findById(targetTagId)).thenReturn(Optional.empty());

		// When
		mergeTagUseCase.merge(sourceTagId, targetTagId);

		// Then
		verify(bookRepository, Mockito.never()).saveAll(Mockito.anyList());
		verify(tagRepository, Mockito.never()).save(Mockito.any());
		verify(tagRepository, Mockito.never()).delete(Mockito.any());
	}
}
