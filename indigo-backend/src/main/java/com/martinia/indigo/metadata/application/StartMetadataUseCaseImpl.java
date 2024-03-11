package com.martinia.indigo.metadata.application;

import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.domain.model.MetadataProcessEnum;
import com.martinia.indigo.metadata.domain.model.MetadataProcessType;
import com.martinia.indigo.metadata.domain.model.commands.StartFillAuthorsMetadataCommand;
import com.martinia.indigo.metadata.domain.model.commands.StartFillBooksMetadataCommand;
import com.martinia.indigo.metadata.domain.model.commands.StartFillReviewsMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.StartMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Slf4j
@Service
@Transactional
public class StartMetadataUseCaseImpl implements StartMetadataUseCase {

	@Resource
	private CommandBus commandBus;

	@Resource
	private MetadataSingleton metadataSingleton;

	@Override
	public void start(String lang, String type, String entity) {
		log.info("Starting async process");

		if (metadataSingleton.isRunning()) {
			metadataSingleton.stop();
		}
		metadataSingleton.start(type, entity);

		if (type.equals(MetadataProcessType.FULL.name())) {
			switch (entity) {
			case "BOOKS":
				commandBus.execute(StartFillBooksMetadataCommand.builder().override(true).build());
				break;
			case "AUTHORS":
				commandBus.execute(StartFillAuthorsMetadataCommand.builder().override(true).lang(lang).build());
				break;
			case "REVIEWS":
				commandBus.execute(StartFillReviewsMetadataCommand.builder().override(true).lang(lang).build());
				break;
			default:
				throw new RuntimeException();
			}

		}

		if (type.equals(MetadataProcessType.PARTIAL.name())) {
			switch (MetadataProcessEnum.valueOf(entity)) {
			case BOOKS:
				commandBus.execute(StartFillBooksMetadataCommand.builder().override(false).build());
				break;
			case AUTHORS:
				commandBus.execute(StartFillAuthorsMetadataCommand.builder().override(false).lang(lang).build());
				break;
			case REVIEWS:
				commandBus.execute(StartFillReviewsMetadataCommand.builder().override(true).lang(lang).build());
				break;
			default:
				throw new RuntimeException();
			}
		}

	}

}
