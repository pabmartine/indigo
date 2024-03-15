package com.martinia.indigo.book.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.domain.ports.usecases.EditBookUseCase;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.mappers.BookMongoMapper;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import com.martinia.indigo.common.util.ImageUtils;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
public class EditBookUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private EditBookUseCase useCase;

	@MockBean
	private BookRepository bookRepository;

	@MockBean
	private TagRepository tagRepository;

	@MockBean
	private BookMongoMapper bookMongoMapper;

	@MockBean
	private EventBus eventBus;

	@MockBean
	private ImageUtils imageUtils;

	@Test
	public void testEditWithWrongImageFormat() {
		// Given
		Book mockBook = new Book();
		BookMongoEntity bookEntity = new BookMongoEntity();
		bookEntity.setId("id");
		bookEntity.setImage("data:/9");

		when(bookRepository.findById(any())).thenReturn(Optional.of(bookEntity));
		when(bookMongoMapper.domain2Entity(any())).thenReturn(bookEntity);
		when(imageUtils.getBase64Cover(anyString(), eq(true))).thenReturn("base64Image");

		// When
		useCase.edit(mockBook);

		// Then
		verify(bookRepository).save(any());
	}

	@Test
	public void testEditWithWrongImageData() {
		// Given
		Book mockBook = new Book();
		BookMongoEntity bookEntity = new BookMongoEntity();
		bookEntity.setId("id");
		bookEntity.setImage("null");

		when(bookRepository.findById(any())).thenReturn(Optional.of(bookEntity));
		when(bookMongoMapper.domain2Entity(any())).thenReturn(bookEntity);
		when(imageUtils.getBase64Cover(anyString(), eq(true))).thenReturn("base64Image");

		// When
		useCase.edit(mockBook);

		// Then
		verify(bookRepository).save(any());
	}

	@Test
	public void testEditWithWrongSourceImage() {
		// Given
		Book mockBook = new Book();
		BookMongoEntity sourceBookEntity = new BookMongoEntity();
		sourceBookEntity.setImage("sourceImage");
		BookMongoEntity targetBookEntity = new BookMongoEntity();
		targetBookEntity.setImage("null");

		when(bookRepository.findById(any())).thenReturn(Optional.of(sourceBookEntity));
		when(bookMongoMapper.domain2Entity(any())).thenReturn(targetBookEntity);
		when(imageUtils.getBase64Cover(anyString(), eq(true))).thenReturn("base64Image");

		// When
		useCase.edit(mockBook);

		// Then
		verify(bookRepository).save(any());
	}
}
