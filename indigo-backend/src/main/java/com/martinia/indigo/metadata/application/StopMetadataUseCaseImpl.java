package com.martinia.indigo.metadata.application;

import com.martinia.indigo.metadata.application.common.BaseMetadataUseCaseImpl;
import com.martinia.indigo.metadata.domain.ports.usecases.StopMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Slf4j
@Service
@Transactional
public class StopMetadataUseCaseImpl extends BaseMetadataUseCaseImpl implements StopMetadataUseCase {

	@Override
	public void stop() {
		super.stop();
	}

}
