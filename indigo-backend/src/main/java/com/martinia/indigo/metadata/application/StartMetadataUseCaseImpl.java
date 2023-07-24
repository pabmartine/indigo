package com.martinia.indigo.metadata.application;

import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.metadata.application.common.BaseMetadataUseCaseImpl;
import com.martinia.indigo.metadata.domain.model.MetadataProcessEnum;
import com.martinia.indigo.metadata.domain.model.MetadataProcessType;
import com.martinia.indigo.metadata.domain.ports.commands.StartFillAuthorsMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.commands.StartFillBooksMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.commands.StartFillReviewsMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.commands.StartInitialLoadCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.StartMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Slf4j
@Service
@Transactional
public class StartMetadataUseCaseImpl extends BaseMetadataUseCaseImpl implements StartMetadataUseCase {

	@Resource
	private CommandBus commandBus;

	//	@Async
	@Override
	public void start(String lang, String type, String entity) {
		log.info("Starting async process");

		if (metadataSingleton.isRunning()) {
			stop();
		}
		metadataSingleton.start(type, entity);

		if (type.equals(MetadataProcessType.FULL.name())) {
			if (entity.equals(MetadataProcessEnum.LOAD.name())) {
				commandBus.execute(StartInitialLoadCommand.builder().build());
			}
			if (entity.equals(MetadataProcessEnum.BOOKS.name())) {
				commandBus.execute(StartFillBooksMetadataCommand.builder().override(true).build());
			}
			if (entity.equals(MetadataProcessEnum.AUTHORS.name())) {
				commandBus.execute(StartFillAuthorsMetadataCommand.builder().override(true).lang(lang).build());
			}
			if (entity.equals(MetadataProcessEnum.REVIEWS.name())) {
				commandBus.execute(StartFillReviewsMetadataCommand.builder().override(true).lang(lang).build());

			}

		}

		if (type.equals(MetadataProcessType.PARTIAL.name())) {
			if (entity.equals(MetadataProcessEnum.LOAD.name())) {
				noFilledMetadata(lang);
			}
			if (entity.equals(MetadataProcessEnum.BOOKS.name())) {
				commandBus.execute(StartFillBooksMetadataCommand.builder().override(false).build());
			}
			if (entity.equals(MetadataProcessEnum.AUTHORS.name())) {
				commandBus.execute(StartFillAuthorsMetadataCommand.builder().override(false).lang(lang).build());

			}
			if (entity.equals(MetadataProcessEnum.REVIEWS.name())) {
				commandBus.execute(StartFillReviewsMetadataCommand.builder().override(true).lang(lang).build());
			}

		}

	}

}
