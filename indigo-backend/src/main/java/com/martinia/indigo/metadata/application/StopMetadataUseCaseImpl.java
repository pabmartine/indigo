package com.martinia.indigo.metadata.application;

import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.domain.ports.usecases.StopMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Slf4j
@Service
@Transactional
public class StopMetadataUseCaseImpl implements StopMetadataUseCase {

	@Resource
	protected MetadataSingleton metadataSingleton;

	@Override
	public void stop() {
		log.info("Stopping async process");
		metadataSingleton.stop();
	}

}
