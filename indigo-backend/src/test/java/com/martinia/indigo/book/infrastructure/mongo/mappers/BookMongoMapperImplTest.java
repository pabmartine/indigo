package com.martinia.indigo.book.infrastructure.mongo.mappers;

import com.martinia.indigo.book.domain.model.Book;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.ReviewMongo;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import com.martinia.indigo.common.model.Review;
import com.martinia.indigo.common.model.Serie;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookMongoMapperImplTest {

	private BookMongoMapper bookMongoMapper;

	@BeforeEach
	public void setUp() {
		bookMongoMapper = new BookMongoMapperImpl();
	}

	@Test
	public void testDomain2Entity_withValidBook_shouldMapCorrectly() {
		// Arrange
		Book book = new Book();
		book.setId("1");
		book.setTitle("Book Title");
		book.setPath("/path/to/book");
		book.setComment("This is a great book");
		book.setProvider("Provider");
		book.setPubDate(new Date());
		book.setLastModified(new Date());
		book.setPages(300);
		book.setRating(4.5f);
		book.setImage("book.jpg");
		book.setAuthors(List.of("Author1", "Author2"));
		book.setTags(List.of("Tag1", "Tag2"));
		book.setSimilar(List.of("Book1", "Book2"));
		book.setRecommendations(List.of("Book3", "Book4"));
		book.setLanguages(List.of("English", "Spanish"));

		Serie serie = new Serie();
		serie.setIndex(1);
		serie.setName("Serie");
		book.setSerie(serie);

		List<Review> reviews = new ArrayList<>();
		Review review1 = new Review();
		review1.setName("Reviewer1");
		review1.setTitle("Review Title 1");
		review1.setComment("Good book");
		review1.setRating(4);
		review1.setDate(new Date());
		review1.setLastMetadataSync(new Date());
		review1.setProvider("Provider");
		reviews.add(review1);

		Review review2 = new Review();
		review2.setName("Reviewer2");
		review2.setTitle("Review Title 2");
		review2.setComment("Excellent book");
		review2.setRating(5);
		review2.setDate(new Date());
		review2.setLastMetadataSync(new Date());
		review2.setProvider("Provider");
		reviews.add(review2);

		book.setReviews(reviews);
		book.setLastMetadataSync(new Date());

		// Act
		BookMongoEntity bookMongoEntity = bookMongoMapper.domain2Entity(book);

		// Assert
		Assertions.assertEquals(book.getId(), bookMongoEntity.getId());
		Assertions.assertEquals(book.getTitle(), bookMongoEntity.getTitle());
		Assertions.assertEquals(book.getPath(), bookMongoEntity.getPath());
		Assertions.assertEquals(book.getComment(), bookMongoEntity.getComment());
		Assertions.assertEquals(book.getProvider(), bookMongoEntity.getProvider());
		Assertions.assertEquals(book.getSerie().getIndex(), bookMongoEntity.getSerie().getIndex());
		Assertions.assertEquals(book.getSerie().getName(), bookMongoEntity.getSerie().getName());
		Assertions.assertEquals(book.getPubDate(), bookMongoEntity.getPubDate());
		Assertions.assertEquals(book.getLastModified(), bookMongoEntity.getLastModified());
		Assertions.assertEquals(book.getPages(), bookMongoEntity.getPages());
		Assertions.assertEquals(book.getRating(), bookMongoEntity.getRating());
		Assertions.assertEquals(book.getImage(), bookMongoEntity.getImage());
		Assertions.assertEquals(book.getAuthors(), bookMongoEntity.getAuthors());
		Assertions.assertEquals(book.getTags(), bookMongoEntity.getTags());
		Assertions.assertEquals(book.getSimilar(), bookMongoEntity.getSimilar());
		Assertions.assertEquals(book.getRecommendations(), bookMongoEntity.getRecommendations());
		Assertions.assertEquals(book.getLanguages(), bookMongoEntity.getLanguages());
		Assertions.assertEquals(book.getReviews().size(), bookMongoEntity.getReviews().size());
		Assertions.assertEquals(book.getLastMetadataSync(), bookMongoEntity.getLastMetadataSync());
	}

	@Test
	public void testEntity2Domain_withValidBookEntity_shouldMapCorrectly() {
		// Arrange
		BookMongoEntity bookMongoEntity = new BookMongoEntity();
		bookMongoEntity.setId("1");
		bookMongoEntity.setTitle("Book Title");
		bookMongoEntity.setPath("/path/to/book");
		bookMongoEntity.setComment("This is a great book");
		bookMongoEntity.setProvider("Provider");
		bookMongoEntity.setPubDate(new Date());
		bookMongoEntity.setLastModified(new Date());
		bookMongoEntity.setPages(300);
		bookMongoEntity.setRating(4.5f);
		bookMongoEntity.setImage("book.jpg");
		bookMongoEntity.setAuthors(List.of("Author1", "Author2"));
		bookMongoEntity.setTags(List.of("Tag1", "Tag2"));
		bookMongoEntity.setSimilar(List.of("Book1", "Book2"));
		bookMongoEntity.setRecommendations(List.of("Book3", "Book4"));
		bookMongoEntity.setLanguages(List.of("English", "Spanish"));

		SerieMongo serieMongo = new SerieMongo();
		serieMongo.setIndex(1);
		serieMongo.setName("Serie");
		bookMongoEntity.setSerie(serieMongo);

		List<ReviewMongo> reviewMongoList = new ArrayList<>();
		ReviewMongo reviewMongo1 = new ReviewMongo();
		reviewMongo1.setName("Reviewer1");
		reviewMongo1.setTitle("Review Title 1");
		reviewMongo1.setComment("Good book");
		reviewMongo1.setRating(4);
		reviewMongo1.setDate(new Date());
		reviewMongo1.setLastMetadataSync(new Date());
		reviewMongo1.setProvider("Provider");
		reviewMongoList.add(reviewMongo1);

		ReviewMongo reviewMongo2 = new ReviewMongo();
		reviewMongo2.setName("Reviewer2");
		reviewMongo2.setTitle("Review Title 2");
		reviewMongo2.setComment("Excellent book");
		reviewMongo2.setRating(5);
		reviewMongo2.setDate(new Date());
		reviewMongo2.setLastMetadataSync(new Date());
		reviewMongo2.setProvider("Provider");
		reviewMongoList.add(reviewMongo2);

		bookMongoEntity.setReviews(reviewMongoList);
		bookMongoEntity.setLastMetadataSync(new Date());

		// Act
		Book book = bookMongoMapper.entity2Domain(bookMongoEntity);

		// Assert
		Assertions.assertEquals(bookMongoEntity.getId(), book.getId());
		Assertions.assertEquals(bookMongoEntity.getTitle(), book.getTitle());
		Assertions.assertEquals(bookMongoEntity.getPath(), book.getPath());
		Assertions.assertEquals(bookMongoEntity.getComment(), book.getComment());
		Assertions.assertEquals(bookMongoEntity.getProvider(), book.getProvider());
		Assertions.assertEquals(bookMongoEntity.getSerie().getIndex(), book.getSerie().getIndex());
		Assertions.assertEquals(bookMongoEntity.getSerie().getName(), book.getSerie().getName());
		Assertions.assertEquals(bookMongoEntity.getPubDate(), book.getPubDate());
		Assertions.assertEquals(bookMongoEntity.getLastModified(), book.getLastModified());
		Assertions.assertEquals(bookMongoEntity.getPages(), book.getPages());
		Assertions.assertEquals(bookMongoEntity.getRating(), book.getRating());
		Assertions.assertEquals(bookMongoEntity.getImage(), book.getImage());
		Assertions.assertEquals(bookMongoEntity.getAuthors(), book.getAuthors());
		Assertions.assertEquals(bookMongoEntity.getTags(), book.getTags());
		Assertions.assertEquals(bookMongoEntity.getSimilar(), book.getSimilar());
		Assertions.assertEquals(bookMongoEntity.getRecommendations(), book.getRecommendations());
		Assertions.assertEquals(bookMongoEntity.getLanguages(), book.getLanguages());
		Assertions.assertEquals(bookMongoEntity.getReviews().size(), book.getReviews().size());
		Assertions.assertEquals(bookMongoEntity.getLastMetadataSync(), book.getLastMetadataSync());
	}

	@Test
	public void testEntity2Domain_withNullBookEntity_shouldReturnNull() {
		// Act
		Book book = bookMongoMapper.entity2Domain(null);

		// Assert
		Assertions.assertNull(book);
	}

	@Test
	public void testDomain2Entity_withListOfBooks_shouldMapCorrectly() {
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
		List<BookMongoEntity> bookMongoEntities = bookMongoMapper.domains2Entities(books);

		// Assert
		Assertions.assertEquals(books.size(), bookMongoEntities.size());

		BookMongoEntity bookMongoEntity1 = bookMongoEntities.get(0);
		Assertions.assertEquals(book1.getId(), bookMongoEntity1.getId());
		Assertions.assertEquals(book1.getTitle(), bookMongoEntity1.getTitle());

		BookMongoEntity bookMongoEntity2 = bookMongoEntities.get(1);
		Assertions.assertEquals(book2.getId(), bookMongoEntity2.getId());
		Assertions.assertEquals(book2.getTitle(), bookMongoEntity2.getTitle());
	}

	@Test
	public void testEntities2Domains_withListOfBookEntities_shouldMapCorrectly() {
		// Arrange
		List<BookMongoEntity> bookMongoEntities = new ArrayList<>();
		BookMongoEntity bookMongoEntity1 = new BookMongoEntity();
		bookMongoEntity1.setId("1");
		bookMongoEntity1.setTitle("Book 1");
		bookMongoEntities.add(bookMongoEntity1);

		BookMongoEntity bookMongoEntity2 = new BookMongoEntity();
		bookMongoEntity2.setId("2");
		bookMongoEntity2.setTitle("Book 2");
		bookMongoEntities.add(bookMongoEntity2);

		// Act
		List<Book> books = bookMongoMapper.entities2Domains(bookMongoEntities);

		// Assert
		Assertions.assertEquals(bookMongoEntities.size(), books.size());

		Book book1 = books.get(0);
		Assertions.assertEquals(bookMongoEntity1.getId(), book1.getId());
		Assertions.assertEquals(bookMongoEntity1.getTitle(), book1.getTitle());

		Book book2 = books.get(1);
		Assertions.assertEquals(bookMongoEntity2.getId(), book2.getId());
		Assertions.assertEquals(bookMongoEntity2.getTitle(), book2.getTitle());
	}

	@Test
	public void testEntities2Domains_withNullListOfBookEntities_shouldReturnNull() {
		// Act
		List<Book> books = bookMongoMapper.entities2Domains(null);

		// Assert
		Assertions.assertNull(books);
	}
}
