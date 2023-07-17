package com.martinia.indigo.common.infrastructure.api.mappers;

import static org.junit.jupiter.api.Assertions.*;

import com.martinia.indigo.common.infrastructure.api.model.ReviewDto;
import com.martinia.indigo.common.model.Review;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReviewDtoMapperImplTest {

	private ReviewDtoMapper reviewDtoMapper;

	@BeforeEach
	public void setUp() {
		reviewDtoMapper = new ReviewDtoMapperImpl();
	}

	@Test
	public void testDomain2Dto_withValidReview_shouldMapCorrectly() {
		// Arrange
		Review review = new Review();
		review.setName("John Doe");
		review.setTitle("Great Book");
		review.setComment("This book is amazing!");
		review.setRating(5);
		review.setDate(new Date());

		// Act
		ReviewDto reviewDto = reviewDtoMapper.domain2Dto(review);

		// Assert
		Assertions.assertEquals(review.getName(), reviewDto.getName());
		Assertions.assertEquals(review.getTitle(), reviewDto.getTitle());
		Assertions.assertEquals(review.getComment(), reviewDto.getComment());
		Assertions.assertEquals(review.getRating(), reviewDto.getRating());
		Assertions.assertEquals(review.getDate(), reviewDto.getDate());
	}

	@Test
	public void testDomain2Dto_withNullReview_shouldReturnNull() {
		// Act
		ReviewDto reviewDto = reviewDtoMapper.domain2Dto(null);

		// Assert
		Assertions.assertNull(reviewDto);
	}

	@Test
	public void testDomains2Dtos_withValidReviewList_shouldMapCorrectly() {
		// Arrange
		List<Review> reviewList = new ArrayList<>();
		Review review1 = new Review();
		review1.setName("John Doe");
		review1.setTitle("Great Book");
		review1.setComment("This book is amazing!");
		review1.setRating(5);
		review1.setDate(new Date());
		reviewList.add(review1);

		Review review2 = new Review();
		review2.setName("Jane Smith");
		review2.setTitle("Awesome Read");
		review2.setComment("Highly recommended!");
		review2.setRating(4);
		review2.setDate(new Date());
		reviewList.add(review2);

		// Act
		List<ReviewDto> reviewDtoList = reviewDtoMapper.domains2Dtos(reviewList);

		// Assert
		Assertions.assertEquals(reviewList.size(), reviewDtoList.size());

		for (int i = 0; i < reviewList.size(); i++) {
			Review review = reviewList.get(i);
			ReviewDto reviewDto = reviewDtoList.get(i);
			Assertions.assertEquals(review.getName(), reviewDto.getName());
			Assertions.assertEquals(review.getTitle(), reviewDto.getTitle());
			Assertions.assertEquals(review.getComment(), reviewDto.getComment());
			Assertions.assertEquals(review.getRating(), reviewDto.getRating());
			Assertions.assertEquals(review.getDate(), reviewDto.getDate());
		}
	}

	@Test
	public void testDomains2Dtos_withNullReviewList_shouldReturnNull() {
		// Act
		List<ReviewDto> reviewDtoList = reviewDtoMapper.domains2Dtos(null);

		// Assert
		Assertions.assertNull(reviewDtoList);
	}

	@Test
	public void testDto2Domain_withValidReviewDto_shouldMapCorrectly() {
		// Arrange
		ReviewDto reviewDto = new ReviewDto();
		reviewDto.setName("John Doe");
		reviewDto.setTitle("Great Book");
		reviewDto.setComment("This book is amazing!");
		reviewDto.setRating(5);
		reviewDto.setDate(new Date());

		// Act
		Review review = reviewDtoMapper.dto2domain(reviewDto);

		// Assert
		Assertions.assertEquals(reviewDto.getName(), review.getName());
		Assertions.assertEquals(reviewDto.getTitle(), review.getTitle());
		Assertions.assertEquals(reviewDto.getComment(), review.getComment());
		Assertions.assertEquals(reviewDto.getRating(), review.getRating());
		Assertions.assertEquals(reviewDto.getDate(), review.getDate());
	}

	@Test
	public void testDto2Domain_withNullReviewDto_shouldReturnNull() {
		// Act
		Review review = reviewDtoMapper.dto2domain(null);

		// Assert
		Assertions.assertNull(review);
	}

	@Test
	public void testDtos2Domains_withValidReviewDtoList_shouldMapCorrectly() {
		// Arrange
		List<ReviewDto> reviewDtoList = new ArrayList<>();
		ReviewDto reviewDto1 = new ReviewDto();
		reviewDto1.setName("John Doe");
		reviewDto1.setTitle("Great Book");
		reviewDto1.setComment("This book is amazing!");
		reviewDto1.setRating(5);
		reviewDto1.setDate(new Date());
		reviewDtoList.add(reviewDto1);

		ReviewDto reviewDto2 = new ReviewDto();
		reviewDto2.setName("Jane Smith");
		reviewDto2.setTitle("Awesome Read");
		reviewDto2.setComment("Highly recommended!");
		reviewDto2.setRating(4);
		reviewDto2.setDate(new Date());
		reviewDtoList.add(reviewDto2);

		// Act
		List<Review> reviewList = reviewDtoMapper.dtos2domains(reviewDtoList);

		// Assert
		Assertions.assertEquals(reviewDtoList.size(), reviewList.size());

		for (int i = 0; i < reviewDtoList.size(); i++) {
			ReviewDto reviewDto = reviewDtoList.get(i);
			Review review = reviewList.get(i);
			Assertions.assertEquals(reviewDto.getName(), review.getName());
			Assertions.assertEquals(reviewDto.getTitle(), review.getTitle());
			Assertions.assertEquals(reviewDto.getComment(), review.getComment());
			Assertions.assertEquals(reviewDto.getRating(), review.getRating());
			Assertions.assertEquals(reviewDto.getDate(), review.getDate());
		}
	}

	@Test
	public void testDtos2Domains_withNullReviewDtoList_shouldReturnNull() {
		// Act
		List<Review> reviewList = reviewDtoMapper.dtos2domains(null);

		// Assert
		Assertions.assertNull(reviewList);
	}
}
