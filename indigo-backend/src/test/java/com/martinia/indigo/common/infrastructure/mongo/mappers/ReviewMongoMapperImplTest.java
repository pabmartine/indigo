package com.martinia.indigo.common.infrastructure.mongo.mappers;

import com.martinia.indigo.book.infrastructure.mongo.entities.ReviewMongo;
import com.martinia.indigo.common.domain.model.Review;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReviewMongoMapperImplTest {

	private ReviewMongoMapper reviewMongoMapper;

	@BeforeEach
	public void setUp() {
		reviewMongoMapper = new ReviewMongoMapperImpl();
	}

	@Test
	public void testDomain2Entity_withValidReview_shouldMapCorrectly() {
		// Arrange
		Review review = new Review();
		review.setName("John Doe");
		review.setTitle("Great Book");
		review.setComment("This book is amazing!");
		review.setRating(5);
		review.setDate(new Date());
		review.setLastMetadataSync(new Date());
		review.setProvider("Goodreads");

		// Act
		ReviewMongo reviewMongo = reviewMongoMapper.domain2Entity(review);

		// Assert
		Assertions.assertEquals(review.getName(), reviewMongo.getName());
		Assertions.assertEquals(review.getTitle(), reviewMongo.getTitle());
		Assertions.assertEquals(review.getComment(), reviewMongo.getComment());
		Assertions.assertEquals(review.getRating(), reviewMongo.getRating());
		Assertions.assertEquals(review.getDate(), reviewMongo.getDate());
		Assertions.assertEquals(review.getLastMetadataSync(), reviewMongo.getLastMetadataSync());
		Assertions.assertEquals(review.getProvider(), reviewMongo.getProvider());
	}

	@Test
	public void testDomain2Entity_withNullReview_shouldReturnNull() {
		// Act
		ReviewMongo reviewMongo = reviewMongoMapper.domain2Entity(null);

		// Assert
		Assertions.assertNull(reviewMongo);
	}

	@Test
	public void testDomains2Entities_withValidReviewList_shouldMapCorrectly() {
		// Arrange
		List<Review> reviewList = new ArrayList<>();
		Review review1 = new Review();
		review1.setName("John Doe");
		review1.setTitle("Great Book");
		review1.setComment("This book is amazing!");
		review1.setRating(5);
		review1.setDate(new Date());
		review1.setLastMetadataSync(new Date());
		review1.setProvider("Goodreads");
		reviewList.add(review1);

		Review review2 = new Review();
		review2.setName("Jane Smith");
		review2.setTitle("Awesome Read");
		review2.setComment("Highly recommended!");
		review2.setRating(4);
		review2.setDate(new Date());
		review2.setLastMetadataSync(new Date());
		review2.setProvider("Amazon");
		reviewList.add(review2);

		// Act
		List<ReviewMongo> reviewMongoList = reviewMongoMapper.domains2Entities(reviewList);

		// Assert
		Assertions.assertEquals(reviewList.size(), reviewMongoList.size());

		for (int i = 0; i < reviewList.size(); i++) {
			Review review = reviewList.get(i);
			ReviewMongo reviewMongo = reviewMongoList.get(i);
			Assertions.assertEquals(review.getName(), reviewMongo.getName());
			Assertions.assertEquals(review.getTitle(), reviewMongo.getTitle());
			Assertions.assertEquals(review.getComment(), reviewMongo.getComment());
			Assertions.assertEquals(review.getRating(), reviewMongo.getRating());
			Assertions.assertEquals(review.getDate(), reviewMongo.getDate());
			Assertions.assertEquals(review.getLastMetadataSync(), reviewMongo.getLastMetadataSync());
			Assertions.assertEquals(review.getProvider(), reviewMongo.getProvider());
		}
	}

	@Test
	public void testDomains2Entities_withNullReviewList_shouldReturnNull() {
		// Act
		List<ReviewMongo> reviewMongoList = reviewMongoMapper.domains2Entities(null);

		// Assert
		Assertions.assertNull(reviewMongoList);
	}
}
