package com.martinia.indigo.book.application.cover;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.domain.ports.usecases.cover.FindBookCoverByIdUseCase;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class FindBookCoverByIdUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private FindBookCoverByIdUseCase useCase;

	@Test
	void shouldReturnCoverBytesWhenImageExists() {
		BookMongoEntity book = BookMongoEntity.builder()
				.title("Test book")
				.path("/tmp/book.epub")
				.image(Base64.getEncoder().encodeToString("cover-bytes".getBytes(StandardCharsets.UTF_8)))
				.build();
		book = bookRepository.save(book);

		Optional<byte[]> cover = useCase.getCover(book.getId());

		assertThat(cover).isPresent();
		assertThat(new String(cover.get(), StandardCharsets.UTF_8)).isEqualTo("cover-bytes");
	}

	@Test
	void shouldReturnEmptyWhenBookHasNoImage() {
		BookMongoEntity book = BookMongoEntity.builder()
				.title("No cover")
				.path("/tmp/no-cover.epub")
				.image(null)
				.build();
		book = bookRepository.save(book);

		Optional<byte[]> cover = useCase.getCover(book.getId());

		assertThat(cover).isNotPresent();
	}

	@Test
	void shouldReturnEmptyWhenBookDoesNotExist() {
		Optional<byte[]> cover = useCase.getCover("507f1f77bcf86cd799439011");
		assertThat(cover).isNotPresent();
	}
}
