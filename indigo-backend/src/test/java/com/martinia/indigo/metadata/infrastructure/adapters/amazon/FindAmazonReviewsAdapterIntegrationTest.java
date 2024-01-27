package com.martinia.indigo.metadata.infrastructure.adapters.amazon;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import com.martinia.indigo.metadata.domain.model.commands.FindAuthorMetadataCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FindAmazonReviewsAdapterIntegrationTest extends BaseIndigoIntegrationTest {
	private AuthorMongoEntity authorMongoEntity;
	@BeforeEach
	void init(){
		insertAuthor();
		insertBook();
	}
	@Test
	void findAmazonReviewsAuthorNotFound() {
		//Given
		FindAuthorMetadataCommand command = FindAuthorMetadataCommand.builder()
				.authorId("authorId")
				.override(true)
				.lastExecution(System.currentTimeMillis())
				.lang("lang")
				.build();
		//When
		commandBus.executeAndWait(command);
		//Then
		assertTrue(authorRepository.findById(command.getAuthorId()).isEmpty());
	}

	@Test
	void findAmazonReviewsOverrideFalseAndNotRefresh() {
		//Given
		FindAuthorMetadataCommand command = FindAuthorMetadataCommand.builder()
				.authorId("id")
				.override(false)
				.lastExecution(System.currentTimeMillis())
				.lang("lang")
				.build();

		insertAuthor();
		//When
		commandBus.executeAndWait(command);
		//Then
		assertFalse(authorRepository.findById(command.getAuthorId()).isEmpty());
	}

	@Test
	void findAmazonReviewsNotFound() {
		//Given
		FindAuthorMetadataCommand command = FindAuthorMetadataCommand.builder()
				.authorId("id")
				.override(true)
				.lastExecution(System.currentTimeMillis())
				.lang("lang")
				.build();

		insertAuthor();

		Mockito.doReturn(null).when(findWikipediaAuthorPort).findAuthor(Mockito.anyString(),
				Mockito.anyString(), Mockito.anyInt());
		Mockito.doReturn(null).when(findGoodReadsAuthorPort).findAuthor(Mockito.anyString(),
				Mockito.anyString());

		//When
		commandBus.executeAndWait(command);
		//Then
		Optional<AuthorMongoEntity> optEntity = authorRepository.findById(command.getAuthorId());
		assertTrue(optEntity.isPresent());
		AuthorMongoEntity entity = optEntity.get();
		assertEquals(authorMongoEntity.getId(), entity.getId());
		assertEquals(authorMongoEntity.getName(), entity.getName());
		assertEquals(authorMongoEntity.getSort(), entity.getSort());
		assertNull(entity.getDescription());
		assertNull(entity.getProvider());
		assertNull(entity.getImage());
		assertTrue(authorMongoEntity.getLastMetadataSync().before(entity.getLastMetadataSync()));
	}


	@Test
	void findAmazonReviewsWikipediaFound() {
		//Given
		FindAuthorMetadataCommand command = FindAuthorMetadataCommand.builder()
				.authorId("id")
				.override(true)
				.lastExecution(System.currentTimeMillis())
				.lang("lang")
				.build();

		insertAuthor();

		String[] wikipedia = new String[3];
		wikipedia[0] = "new description";
		wikipedia[1] = "new image";
		wikipedia[2] = "new provider";
		Mockito.doReturn(wikipedia).when(findWikipediaAuthorPort).findAuthor(Mockito.anyString(),
				Mockito.anyString(), Mockito.anyInt());
		Mockito.doReturn(null).when(findGoodReadsAuthorPort).findAuthor(Mockito.anyString(),
				Mockito.anyString());

		//When
		commandBus.executeAndWait(command);
		//Then
		Optional<AuthorMongoEntity> optEntity = authorRepository.findById(command.getAuthorId());
		assertTrue(optEntity.isPresent());
		AuthorMongoEntity entity = optEntity.get();
		assertEquals(authorMongoEntity.getId(), entity.getId());
		assertEquals(authorMongoEntity.getName(), entity.getName());
		assertEquals(authorMongoEntity.getSort(), entity.getSort());
		assertEquals(wikipedia[0], entity.getDescription());
		assertEquals(wikipedia[2], entity.getProvider());
		assertEquals(wikipedia[1], entity.getImage());
		assertTrue(authorMongoEntity.getLastMetadataSync().before(entity.getLastMetadataSync()));
	}

	@Test
	void findAmazonReviewsGoodreadsFound() {
		//Given
		FindAuthorMetadataCommand command = FindAuthorMetadataCommand.builder()
				.authorId("id")
				.override(true)
				.lastExecution(System.currentTimeMillis())
				.lang("lang")
				.build();

		insertAuthor();

		String[] goodReads = new String[3];
		goodReads[0] = "new description";
		goodReads[1] = "new image";
		goodReads[2] = "new provider";

		Mockito.doReturn(null).when(findWikipediaAuthorPort).findAuthor(Mockito.anyString(),
				Mockito.anyString(), Mockito.anyInt());
		Mockito.doReturn(goodReads).when(findGoodReadsAuthorPort).findAuthor(Mockito.anyString(),
				Mockito.anyString());


		//When
		commandBus.executeAndWait(command);
		//Then
		Optional<AuthorMongoEntity> optEntity = authorRepository.findById(command.getAuthorId());
		assertTrue(optEntity.isPresent());
		AuthorMongoEntity entity = optEntity.get();
		assertEquals(authorMongoEntity.getId(), entity.getId());
		assertEquals(authorMongoEntity.getName(), entity.getName());
		assertEquals(authorMongoEntity.getSort(), entity.getSort());
//		assertEquals(goodReads[0], entity.getDescription());
//		assertEquals(goodReads[2], entity.getProvider());
//		assertEquals(goodReads[1], entity.getImage());
		assertTrue(authorMongoEntity.getLastMetadataSync().before(entity.getLastMetadataSync()));
	}

	private void insertAuthor() {
		final Map<String, Integer> map = new HashMap<>();
		map.put("es", 1);
		authorMongoEntity = AuthorMongoEntity.builder()
				.id("id")
				.name("AA. VV.")
				.sort("AA. VV.")
				.numBooks(NumBooksMongo.builder().total(1).languages(map).build())
				.image("::image::")
				.description("::description::")
				.provider("::provider::")
				.lastMetadataSync(new Date())
				.build();
		authorRepository.save(authorMongoEntity);
	}

	private void insertBook() {
		BookMongoEntity bookMongoEntity = BookMongoEntity.builder()
				.id("id")
				.title("title")
				.path("path")
				.languages(List.of("es"))
				.similar(Arrays.asList("similar"))
				.authors(Arrays.asList("AA. VV."))
				.serie(SerieMongo.builder().index(1).name("Serie1").build())
				.pages(100)
				.tags(Arrays.asList("tag"))
				.image("::image::")
				.build();
		bookRepository.save(bookMongoEntity);
	}
}