package com.martinia.indigo.metadata.application;

import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.configuration.infrastructure.mongo.entities.ConfigurationMongoEntity;
import com.martinia.indigo.metadata.application.common.BaseMetadataUseCaseImpl;
import com.martinia.indigo.metadata.domain.ports.commands.StartInitialLoadCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.StartMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Slf4j
@Service
public class StartMetadataUseCaseImpl extends BaseMetadataUseCaseImpl implements StartMetadataUseCase {

	@Resource
	private CommandBus commandBus;

	//	@Async
	@Override
	public void start(String lang, String type, String entity) {
		log.info("Starting async process");

		if (Optional.ofNullable(goodreads).isEmpty()) {
			goodreads = configurationRepository.findByKey("goodreads.key").map(ConfigurationMongoEntity::getValue).orElse(null);
		}

		if (Optional.ofNullable(pullTime).isEmpty()) {
			pullTime = configurationRepository.findByKey("metadata.pull")
					.map(configuration -> Long.parseLong(configuration.getValue()))
					.orElse(null);
		}

		if (metadataSingleton.isRunning()) {
			stop();
		}
		metadataSingleton.start(type, entity);

		if (type.equals("full")) {
			if (entity.equals("all")) {
				//				initialLoad(lang);
				commandBus.execute(StartInitialLoadCommand.builder().build());
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

//		if (metadataSingleton.isRunning()) {
//			stop();
//		}
	}

}
