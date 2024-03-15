package com.martinia.indigo.common.infrastructure.mongo.mappers;

import com.martinia.indigo.common.infrastructure.mongo.entities.ViewMongoEntity;
import com.martinia.indigo.common.domain.model.View;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class ViewMongoMapperImplTest {

	private ViewMongoMapper viewMongoMapper;

	@BeforeEach
	public void setUp() {
		viewMongoMapper = new ViewMongoMapperImpl();
	}

	@Test
	public void testDomain2Entity_withValidView_shouldMapCorrectly() {
		// Arrange
		View view = new View();
		view.setId("1");
		view.setUser("user1");
		view.setBook("book1");
		view.setViewDate(new Date());

		// Act
		ViewMongoEntity viewMongoEntity = viewMongoMapper.domain2Entity(view);

		// Assert
		Assertions.assertEquals(view.getId(), viewMongoEntity.getId());
		Assertions.assertEquals(view.getUser(), viewMongoEntity.getUser());
		Assertions.assertEquals(view.getBook(), viewMongoEntity.getBook());
		Assertions.assertEquals(view.getViewDate(), viewMongoEntity.getViewDate());
	}

	@Test
	public void testDomain2Entity_withNullView_shouldReturnNull() {
		// Act
		ViewMongoEntity viewMongoEntity = viewMongoMapper.domain2Entity(null);

		// Assert
		Assertions.assertNull(viewMongoEntity);
	}
}
