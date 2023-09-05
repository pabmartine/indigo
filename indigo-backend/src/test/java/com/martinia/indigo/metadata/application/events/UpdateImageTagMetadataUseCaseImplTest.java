package com.martinia.indigo.metadata.application.events;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.tag.domain.ports.usecases.UpdateImageTagUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UpdateImageTagMetadataUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private UpdateImageTagMetadataUseCaseImpl useCase;

	@MockBean
	private BookRepository bookRepository;

	@MockBean
	private UpdateImageTagUseCase updateImageTagUseCase;

	@Test
	public void testUpdateImage() {
		// Given
		String bookId = "testBookId";
		BookMongoEntity mockBook = new BookMongoEntity(); // You need to create a Book instance for mocking
		mockBook.setTags(Collections.singletonList("tag1"));

		when(bookRepository.findById(bookId)).thenReturn(Optional.of(mockBook));

		// When
		useCase.updateImage(bookId);

		// Then
		// Verify that updateImageTagUseCase.updateImage was called with the expected tag
		verify(updateImageTagUseCase).updateImage("tag1");
	}

	@Test
	public void testUpdateImageWithNonExistingBook() {
		// Given
		String bookId = "nonExistingBookId";

		when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

		// When
		useCase.updateImage(bookId);

		// Then
		// Ensure that updateImageTagUseCase.updateImage was not called
		verifyNoInteractions(updateImageTagUseCase);
	}

}