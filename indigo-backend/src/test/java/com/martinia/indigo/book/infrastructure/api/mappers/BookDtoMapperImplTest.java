package com.martinia.indigo.book.infrastructure.api.mappers;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.infrastructure.api.model.BookDto;
import com.martinia.indigo.common.infrastructure.api.model.ReviewDto;
import com.martinia.indigo.common.model.Review;
import com.martinia.indigo.common.model.Serie;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class BookDtoMapperImplTest {

	private BookDtoMapper bookDtoMapper;

	@BeforeEach
	public void setUp() {
		bookDtoMapper = new BookDtoMapperImpl();
	}

	@Test
	public void testDomain2Dto_withValidBook_shouldMapCorrectly() {
		// Arrange
		Book book = new Book();
		book.setId("1");
		book.setTitle("Book Title");
		book.setPath("/path/to/book");
		book.setComment("This is a great book");
		book.setProvider("Provider");
		book.setSerie(new Serie(1, "Serie"));
		book.setPubDate(new Date());
		book.setLastModified(new Date());
		book.setPages(300);
		book.setRating(4.5f);
		book.setImage("book.jpg");
		book.setAuthors(Arrays.asList("Author1", "Author2"));
		book.setTags(Arrays.asList("Tag1", "Tag2"));
		book.setSimilar(Arrays.asList("Book1", "Book2"));
		book.setRecommendations(Arrays.asList("Book3", "Book4"));
		book.setLanguages(Arrays.asList("English", "Spanish"));

		List<Review> reviews = new ArrayList<>();
		Review review1 = new Review();
		review1.setName("Reviewer1");
		review1.setTitle("Review Title 1");
		review1.setComment("Good book");
		review1.setRating(4);
		review1.setDate(new Date());
		reviews.add(review1);

		Review review2 = new Review();
		review2.setName("Reviewer2");
		review2.setTitle("Review Title 2");
		review2.setComment("Excellent book");
		review2.setRating(5);
		review2.setDate(new Date());
		reviews.add(review2);

		book.setReviews(reviews);

		// Act
		BookDto bookDto = bookDtoMapper.domain2Dto(book);

		// Assert
		Assertions.assertEquals(book.getId(), bookDto.getId());
		Assertions.assertEquals(book.getTitle(), bookDto.getTitle());
		Assertions.assertEquals(book.getPath(), bookDto.getPath());
		Assertions.assertEquals(book.getComment(), bookDto.getComment());
		Assertions.assertEquals(book.getProvider(), bookDto.getProvider());
		Assertions.assertEquals(book.getSerie().getIndex(), bookDto.getSerie().getIndex());
		Assertions.assertEquals(book.getSerie().getName(), bookDto.getSerie().getName());
		Assertions.assertEquals(new SimpleDateFormat("dd/MM/yyyy").format(book.getPubDate()), bookDto.getPubDate());
		Assertions.assertEquals(new SimpleDateFormat("dd/MM/yyyy").format(book.getLastModified()), bookDto.getLastModified());
		Assertions.assertEquals(book.getPages(), bookDto.getPages());
		Assertions.assertEquals(book.getRating(), bookDto.getRating());
		Assertions.assertEquals(book.getImage(), bookDto.getImage());
		Assertions.assertEquals(book.getAuthors(), bookDto.getAuthors());
		Assertions.assertEquals(book.getTags(), bookDto.getTags());
		Assertions.assertEquals(book.getSimilar(), bookDto.getSimilar());
		Assertions.assertEquals(book.getRecommendations(), bookDto.getRecommendations());
		Assertions.assertEquals(book.getLanguages(), bookDto.getLanguages());
		Assertions.assertEquals(book.getReviews().size(), bookDto.getReviews().size());

		ReviewDto reviewDto1 = bookDto.getReviews().get(0);
		Assertions.assertEquals(review1.getName(), reviewDto1.getName());
		Assertions.assertEquals(review1.getTitle(), reviewDto1.getTitle());
		Assertions.assertEquals(review1.getComment(), reviewDto1.getComment());
		Assertions.assertEquals(review1.getRating(), reviewDto1.getRating());
		Assertions.assertEquals(review1.getDate(), reviewDto1.getDate());

		ReviewDto reviewDto2 = bookDto.getReviews().get(1);
		Assertions.assertEquals(review2.getName(), reviewDto2.getName());
		Assertions.assertEquals(review2.getTitle(), reviewDto2.getTitle());
		Assertions.assertEquals(review2.getComment(), reviewDto2.getComment());
		Assertions.assertEquals(review2.getRating(), reviewDto2.getRating());
		Assertions.assertEquals(review2.getDate(), reviewDto2.getDate());
	}

	@Test
	public void testDomain2Dto_withNullBook_shouldReturnNull() {
		// Act
		BookDto bookDto = bookDtoMapper.domain2Dto(null);

		// Assert
		Assertions.assertNull(bookDto);
	}

	@Test
	public void testDomains2Dtos_withListOfBooks_shouldMapCorrectly() {
		// Arrange
		List<Book> books = new ArrayList<>();
		Book book1 = new Book();
		book1.setId("1");
		book1.setTitle("Book 1");
		books.add(book1);

		Book book2 = new Book();
		book2.setId("2");
		book2.setTitle("Book 2");
		books.add(book2);

		// Act
		List<BookDto> bookDtos = bookDtoMapper.domains2Dtos(books);

		// Assert
		Assertions.assertEquals(books.size(), bookDtos.size());

		BookDto bookDto1 = bookDtos.get(0);
		Assertions.assertEquals(book1.getId(), bookDto1.getId());
		Assertions.assertEquals(book1.getTitle(), bookDto1.getTitle());

		BookDto bookDto2 = bookDtos.get(1);
		Assertions.assertEquals(book2.getId(), bookDto2.getId());
		Assertions.assertEquals(book2.getTitle(), bookDto2.getTitle());
	}

	@Test
	public void testDomains2Dtos_withNullListOfBooks_shouldReturnNull() {
		// Act
		List<BookDto> bookDtos = bookDtoMapper.domains2Dtos(null);

		// Assert
		Assertions.assertNull(bookDtos);
	}
}
