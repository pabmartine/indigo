package com.martinia.indigo.metadata.application;

import com.martinia.indigo.metadata.application.common.BaseMetadataUseCaseImpl;
import com.martinia.indigo.metadata.domain.service.StopMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StopMetadataUseCaseImpl extends BaseMetadataUseCaseImpl implements StopMetadataUseCase {

	@Override
	public void stop() {
		super.stop();
	}

}
