package com.martinia.indigo.serie.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.SerieMongo;
import com.martinia.indigo.serie.domain.ports.usecases.FindCoverSerieUseCase;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FindCoverSerieUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindCoverSerieUseCase findCoverSerieUseCase;

	@Resource
	private BookRepository bookRepository;

	@Test
	void shouldReturnCoverBytesWhenSerieExists() {
		BookMongoEntity entity = BookMongoEntity.builder()
				.title("Serie Book")
				.path("/tmp/serie-book.epub")
				.languages(List.of("es"))
				.similar(List.of())
				.authors(List.of("Author"))
				.serie(SerieMongo.builder().name("Serie X").index(0).build())
				.pages(100)
				.tags(List.of("tag"))
				.image(Base64.getEncoder().encodeToString("serie-cover".getBytes(StandardCharsets.UTF_8)))
				.build();
		bookRepository.save(entity);

		byte[] cover = findCoverSerieUseCase.getCover("Serie X");

		assertThat(cover).isNotNull();
		assertThat(new String(cover, StandardCharsets.UTF_8)).isEqualTo("serie-cover");
	}

	@Test
	void shouldReturnEmptyBytesWhenSerieNotFound() {
		byte[] cover = findCoverSerieUseCase.getCover("unknown");
		assertThat(cover).isEmpty();
	}
}
