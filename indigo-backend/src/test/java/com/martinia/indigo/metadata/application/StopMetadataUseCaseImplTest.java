package com.martinia.indigo.metadata.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.domain.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.domain.service.StopMetadataUseCase;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class StopMetadataUseCaseImplTest extends BaseIndigoTest {

	@Resource
	private MetadataSingleton metadataSingleton;
	@Resource
	private StopMetadataUseCase stopMetadataUseCase;

	@Test
	public void testStop_WhenInvoked_ThenInvokeSuperStop() {
		//Given
		metadataSingleton.start("type", "entity");

		// When
		stopMetadataUseCase.stop();

		// Then
		assertFalse(metadataSingleton.isRunning());
	}
}
