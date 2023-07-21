package com.martinia.indigo.metadata.application.common;

import com.martinia.indigo.common.singletons.MetadataSingleton;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class BaseMetadataUseCaseImpl {

	@Resource
	protected MetadataSingleton metadataSingleton;

	protected void noFilledMetadata(String lang) {

		metadataSingleton.setMessage("obtaining_metadata");

		//		fillMetadataBooks(false);
		//		fillMetadataAuthors(lang, false);

		stop();
	}

	protected void stop() {
		log.info("Stopping async process");
		metadataSingleton.stop();
	}

}
