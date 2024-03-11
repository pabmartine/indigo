package com.martinia.indigo.metadata.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.domain.ports.usecases.FindStatusMetadataUseCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

public class FindStatusMetadataUseCaseImplTest extends BaseIndigoTest {

	@Mock
	private MetadataSingleton mockMetadataSingleton;

	@InjectMocks
	private FindStatusMetadataUseCase findStatusMetadataUseCase = new FindStatusMetadataUseCaseImpl();

	@Test
	public void testGetStatus_WhenCalled_ThenReturnMetadataStatus() {
		// Given
		Map<String, Object> expectedStatus = new HashMap<>();
		expectedStatus.put("type", null);
		expectedStatus.put("entity", null);
		expectedStatus.put("status", false);
		expectedStatus.put("current", 0L);
		expectedStatus.put("total", 0L);
		expectedStatus.put("message", null);

		Mockito.when(mockMetadataSingleton.getType()).thenReturn(null);
		Mockito.when(mockMetadataSingleton.getEntity()).thenReturn(null);
		Mockito.when(mockMetadataSingleton.isRunning()).thenReturn(false);
		Mockito.when(mockMetadataSingleton.getCurrent()).thenReturn(0L);
		Mockito.when(mockMetadataSingleton.getTotal()).thenReturn(0L);
		Mockito.when(mockMetadataSingleton.getMessage()).thenReturn(null);

		// When
		Map<String, Object> status = findStatusMetadataUseCase.getStatus();

		// Then
		Assertions.assertEquals(expectedStatus, status);
		Mockito.verify(mockMetadataSingleton).getType();
		Mockito.verify(mockMetadataSingleton).getEntity();
		Mockito.verify(mockMetadataSingleton).isRunning();
		Mockito.verify(mockMetadataSingleton).getCurrent();
		Mockito.verify(mockMetadataSingleton).getTotal();
		Mockito.verify(mockMetadataSingleton).getMessage();
	}
}
