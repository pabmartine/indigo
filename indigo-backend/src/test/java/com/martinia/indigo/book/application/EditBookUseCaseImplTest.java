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

import jakarta.annotation.Resource;

import java.util.Optional;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
	@MockBean
	private com.martinia.indigo.metadata.application.MetadataActivityService activity;

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

	@Test
	public void editShouldPreserveBackendManagedMetadata() {
		Book mockBook = new Book();
		BookMongoEntity source = new BookMongoEntity();
		source.setIsbn13(List.of("9780306406157"));
		source.setIdentifiers(Map.of("ISBN_13", List.of("9780306406157")));
		source.setOpenLibraryWorkId("OL1W");
		source.setRatingAverage(4.25F);
		source.setRatingsCount(42L);
		source.setRatingProvider("OPEN_LIBRARY");
		source.setRatingUpdatedAt(new Date());
		source.setMetadataMatchStatus("MATCHED");

		BookMongoEntity target = new BookMongoEntity();
		target.setImage("image");
		when(bookRepository.findById(any())).thenReturn(Optional.of(source));
		when(bookMongoMapper.domain2Entity(any())).thenReturn(target);

		useCase.edit(mockBook);

		assertEquals(source.getIsbn13(), target.getIsbn13());
		assertEquals(source.getIdentifiers(), target.getIdentifiers());
		assertEquals(source.getOpenLibraryWorkId(), target.getOpenLibraryWorkId());
		assertEquals(source.getRatingAverage(), target.getRatingAverage());
		assertEquals(source.getRatingsCount(), target.getRatingsCount());
		assertEquals(source.getRatingProvider(), target.getRatingProvider());
		assertEquals(source.getRatingUpdatedAt(), target.getRatingUpdatedAt());
		assertEquals(source.getMetadataMatchStatus(), target.getMetadataMatchStatus());
		verify(bookRepository).save(target);
	}
}
