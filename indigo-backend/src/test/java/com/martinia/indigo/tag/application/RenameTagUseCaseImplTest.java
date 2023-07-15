package com.martinia.indigo.tag.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.domain.ports.usecases.RenameTagUseCase;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RenameTagUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private RenameTagUseCase renameTagUseCase;

	@MockBean
	private TagRepository tagRepository;

	@MockBean
	private BookRepository bookRepository;

	@Test
	public void testRenameTag_TagExists_RenamesTag() {
		// Given
		String sourceTagName = "Source Tag";
		String targetTagName = "Target Tag";

		String sourceTagId = "1";
		String targetTagId = "2";

		TagMongoEntity sourceTagEntity = new TagMongoEntity();
		sourceTagEntity.setId(sourceTagId);
		sourceTagEntity.setName(sourceTagName);

		List<BookMongoEntity> books = new ArrayList<>();
		BookMongoEntity book1 = new BookMongoEntity();
		book1.setId("1");
		book1.setTags(Arrays.asList(sourceTagName, "Another Tag"));
		books.add(book1);
		BookMongoEntity book2 = new BookMongoEntity();
		book2.setId("2");
		book2.setTags(Arrays.asList(sourceTagName, "Yet Another Tag"));
		books.add(book2);

		when(tagRepository.findById(sourceTagId)).thenReturn(Optional.of(sourceTagEntity));
		when(bookRepository.findByTag(sourceTagName)).thenReturn(books);

		// When
		renameTagUseCase.rename(sourceTagId, targetTagName);

		// Then
		verify(bookRepository).saveAll(books);
		verify(tagRepository).save(sourceTagEntity);
	}

	@Test
	public void testRenameTag_TagDoesNotExist_DoesNothing() {
		// Given
		String sourceTagId = "1";
		String targetTagName = "Target Tag";

		when(tagRepository.findById(sourceTagId)).thenReturn(Optional.empty());

		// When
		renameTagUseCase.rename(sourceTagId, targetTagName);

		// Then
		verify(bookRepository, Mockito.never()).saveAll(Mockito.anyList());
		verify(tagRepository, Mockito.never()).save(Mockito.any());
	}
}
