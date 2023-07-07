package com.martinia.indigo.metadata.application;

import com.martinia.indigo.metadata.application.common.BaseMetadataUseCaseImpl;
import com.martinia.indigo.metadata.domain.service.StartMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StartMetadataUseCaseImpl extends BaseMetadataUseCaseImpl implements StartMetadataUseCase {

	@Async
	@Override
	public void start(String lang, String type, String entity) {
		log.info("Starting async process");

		goodreads = configurationRepository.findByKey("goodreads.key").get().getValue();

		pullTime = Long.parseLong(configurationRepository.findByKey("metadata.pull").get().getValue());

		if (metadataSingleton.isRunning()) {
			stop();
		}
		metadataSingleton.start(type, entity);

		if (type.equals("full")) {
			if (entity.equals("all")) {
				initialLoad(lang);
			}
			if (entity.equals("reviews")) {
				fillMetadataReviews(lang, true);
			}
			if (entity.equals("authors")) {
				fillMetadataAuthors(lang, true);
			}
			if (entity.equals("books")) {
				fillMetadataBooks(true);
			}
		}

		if (type.equals("partial")) {
			if (entity.equals("all")) {
				noFilledMetadata(lang);
			}
			if (entity.equals("reviews")) {
				fillMetadataReviews(lang, false);
			}
			if (entity.equals("authors")) {
				fillMetadataAuthors(lang, false);
			}
			if (entity.equals("books")) {
				fillMetadataBooks(false);
			}
		}

		if (metadataSingleton.isRunning()) {
			stop();
		}
	}

}
