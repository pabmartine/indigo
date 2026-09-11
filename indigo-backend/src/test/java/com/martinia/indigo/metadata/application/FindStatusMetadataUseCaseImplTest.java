package com.martinia.indigo.metadata.application;

import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindStatusMetadataUseCaseImplTest {

	@Mock
	private MetadataSingleton metadataSingleton;

	@Mock
	private UploadEpubFilesSingleton uploadEpubFilesSingleton;

	@InjectMocks
	private FindStatusMetadataUseCaseImpl findStatusMetadataUseCase;

	@Test
	void getStatus_ShouldReturnCompleteStatusMap() {
		when(metadataSingleton.getType()).thenReturn("FULL");
		when(metadataSingleton.getEntity()).thenReturn("BOOKS");
		when(metadataSingleton.isRunning()).thenReturn(true);
		when(metadataSingleton.getCurrent()).thenReturn(50L);
		when(metadataSingleton.getTotal()).thenReturn(100L);
		when(metadataSingleton.getMessage()).thenReturn("Processing books");
		when(metadataSingleton.getCompletedAt()).thenReturn(null);

		when(uploadEpubFilesSingleton.getTotal()).thenReturn(200L);
		when(uploadEpubFilesSingleton.getCurentStatus()).thenReturn(75);
		when(uploadEpubFilesSingleton.isRunning()).thenReturn(true);
		when(uploadEpubFilesSingleton.getProcessedItems()).thenReturn(150L);
		when(uploadEpubFilesSingleton.getFailedItems()).thenReturn(2L);
		when(uploadEpubFilesSingleton.getNewBooks()).thenReturn(140L);
		when(uploadEpubFilesSingleton.getUpdatedBooks()).thenReturn(8L);
		when(uploadEpubFilesSingleton.getMoved()).thenReturn(148L);
		when(uploadEpubFilesSingleton.getDeleted()).thenReturn(2L);

		Map<String, Object> result = findStatusMetadataUseCase.getStatus();

		assertThat(result).isNotNull();
		assertThat(result).hasSize(22);
		assertThat(result).containsKeys("found", "notFound", "skipped", "errors", "runs");
		assertThat(result.get("type")).isEqualTo("FULL");
		assertThat(result.get("entity")).isEqualTo("BOOKS");
		assertThat(result.get("status")).isEqualTo(true);
		assertThat(result.get("current")).isEqualTo(50L);
		assertThat(result.get("total")).isEqualTo(100L);
		assertThat(result.get("message")).isEqualTo("Processing books");
		assertThat(result.get("uploadsTotal")).isEqualTo(200L);
		assertThat(result.get("uploadsCurrent")).isEqualTo(75);
		assertThat(result.get("uploadsRunning")).isEqualTo(true);
		assertThat(result.get("uploadsProcessed")).isEqualTo(150L);
		assertThat(result.get("uploadsFailed")).isEqualTo(2L);
		assertThat(result.get("uploadsSucceeded")).isEqualTo(148L);
		assertThat(result.get("uploadsNewBooks")).isEqualTo(140L);
		assertThat(result.get("uploadsUpdatedBooks")).isEqualTo(8L);
		assertThat(result.get("uploadsMoved")).isEqualTo(148L);
		assertThat(result.get("uploadsDeleted")).isEqualTo(2L);
	}

	@Test
	void getStatus_WhenMetadataNotRunning_ShouldReturnNotRunningStatus() {
		when(metadataSingleton.getType()).thenReturn(null);
		when(metadataSingleton.getEntity()).thenReturn(null);
		when(metadataSingleton.isRunning()).thenReturn(false);
		when(metadataSingleton.getCurrent()).thenReturn(0L);
		when(metadataSingleton.getTotal()).thenReturn(0L);
		when(metadataSingleton.getMessage()).thenReturn(null);

		when(uploadEpubFilesSingleton.getTotal()).thenReturn(0L);
		when(uploadEpubFilesSingleton.getCurentStatus()).thenReturn(0);

		Map<String, Object> result = findStatusMetadataUseCase.getStatus();

		assertThat(result).isNotNull();
		assertThat(result.get("status")).isEqualTo(false);
		assertThat(result.get("current")).isEqualTo(0L);
		assertThat(result.get("total")).isEqualTo(0L);
		assertThat(result.get("uploadsTotal")).isEqualTo(0L);
	}

	@Test
	void getStatus_WhenPartialProcess_ShouldReturnPartialStatus() {
		when(metadataSingleton.getType()).thenReturn("PARTIAL");
		when(metadataSingleton.getEntity()).thenReturn("AUTHORS");
		when(metadataSingleton.isRunning()).thenReturn(true);
		when(metadataSingleton.getCurrent()).thenReturn(25L);
		when(metadataSingleton.getTotal()).thenReturn(50L);
		when(metadataSingleton.getMessage()).thenReturn("Processing authors");

		when(uploadEpubFilesSingleton.getTotal()).thenReturn(100L);
		when(uploadEpubFilesSingleton.getCurentStatus()).thenReturn(10);

		Map<String, Object> result = findStatusMetadataUseCase.getStatus();

		assertThat(result.get("type")).isEqualTo("PARTIAL");
		assertThat(result.get("entity")).isEqualTo("AUTHORS");
		assertThat(result.get("status")).isEqualTo(true);
		assertThat(result.get("current")).isEqualTo(25L);
		assertThat(result.get("total")).isEqualTo(50L);
	}
}
