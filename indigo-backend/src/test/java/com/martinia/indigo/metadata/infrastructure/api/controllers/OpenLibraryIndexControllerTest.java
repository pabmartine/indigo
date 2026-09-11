package com.martinia.indigo.metadata.infrastructure.api.controllers;

import com.martinia.indigo.metadata.application.openlibrary.OpenLibraryIndexManager;
import com.martinia.indigo.metadata.domain.model.OpenLibraryIndexJobStatus;
import com.martinia.indigo.metadata.infrastructure.mongo.entities.OpenLibraryIndexJobMongoEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenLibraryIndexControllerTest {
	@Mock
	private OpenLibraryIndexManager indexManager;

	@InjectMocks
	private OpenLibraryIndexController controller;

	@Test
	void returnsTheCurrentStatus() {
		OpenLibraryIndexJobMongoEntity job = job(OpenLibraryIndexJobStatus.DOWNLOADING_EDITIONS);
		when(indexManager.status()).thenReturn(job);

		assertThat(controller.status()).isSameAs(job);
		verify(indexManager).status();
	}

	@Test
	void startsANewBuildAsynchronously() {
		OpenLibraryIndexJobMongoEntity job = job(OpenLibraryIndexJobStatus.CHECKING_SPACE);
		when(indexManager.start()).thenReturn(job);

		assertThat(controller.start()).satisfies(response -> {
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
			assertThat(response.getBody()).isSameAs(job);
		});
	}

	@Test
	void resumesABuildAsynchronously() {
		OpenLibraryIndexJobMongoEntity job = job(OpenLibraryIndexJobStatus.CHECKING_SPACE);
		when(indexManager.resume()).thenReturn(job);

		assertThat(controller.resume()).satisfies(response -> {
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
			assertThat(response.getBody()).isSameAs(job);
		});
	}

	@Test
	void requestsCancellationAsynchronously() {
		OpenLibraryIndexJobMongoEntity job = job(OpenLibraryIndexJobStatus.DOWNLOADING_RATINGS);
		when(indexManager.cancel()).thenReturn(job);

		assertThat(controller.cancel()).satisfies(response -> {
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
			assertThat(response.getBody()).isSameAs(job);
		});
	}

	private OpenLibraryIndexJobMongoEntity job(final OpenLibraryIndexJobStatus status) {
		return OpenLibraryIndexJobMongoEntity.builder()
				.id(OpenLibraryIndexManager.JOB_ID)
				.status(status)
				.build();
	}
}
