package com.martinia.indigo.metadata.application;

import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.domain.model.BookMetadataScope;
import com.martinia.indigo.metadata.domain.model.DynamicMetadataPolicy;
import com.martinia.indigo.metadata.domain.model.MetadataMergePolicy;
import com.martinia.indigo.metadata.domain.model.MetadataProcessEnum;
import com.martinia.indigo.metadata.domain.model.MetadataProcessType;
import com.martinia.indigo.metadata.domain.model.commands.StartFillAuthorsMetadataCommand;
import com.martinia.indigo.metadata.domain.model.commands.StartFillBooksMetadataCommand;
import com.martinia.indigo.metadata.domain.model.commands.StartFillReviewsMetadataCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.StartMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class StartMetadataUseCaseImpl implements StartMetadataUseCase {

	@Resource
	private CommandBus commandBus;

	@Resource
	private MetadataSingleton metadataSingleton;
	@org.springframework.beans.factory.annotation.Autowired(required = false)
	private com.martinia.indigo.metadata.application.reviews.ReviewQueueService reviewQueue;

	@Override
	public void start(String lang, String type, String entity) {
		log.info("Starting async process");
		try {
			MetadataProcessType.valueOf(type);
			if (MetadataProcessEnum.valueOf(entity) == MetadataProcessEnum.LOAD) {
				log.warn("Unsupported metadata entity: {}", entity);
				return;
			}
		}
		catch (RuntimeException exception) {
			log.warn("Invalid metadata process type/entity: {}/{}", type, entity);
			return;
		}

		if ("REVIEWS".equals(entity) && reviewQueue != null) {
			reviewQueue.start("FULL".equals(type), lang, false);
			return;
		}
		if (metadataSingleton.isRunning()) {
			metadataSingleton.stop();
		}
		final long runId = metadataSingleton.start(type, entity);

		if (type.equals(MetadataProcessType.FULL.name())) {
			switch (entity) {
			case "BOOKS":
				commandBus.execute(bookCommand(BookMetadataScope.ALL, runId));
				break;
			case "AUTHORS":
				commandBus.execute(StartFillAuthorsMetadataCommand.builder().override(true).lang(lang).runId(runId).build());
				break;
			case "REVIEWS":
				commandBus.execute(StartFillReviewsMetadataCommand.builder().override(true).lang(lang).runId(runId).build());
				break;
			default:
				break;
			}

		}

		if (type.equals(MetadataProcessType.PARTIAL.name())) {
			switch (MetadataProcessEnum.valueOf(entity)) {
			case BOOKS:
				commandBus.execute(bookCommand(BookMetadataScope.INCOMPLETE, runId));
				break;
			case AUTHORS:
				commandBus.execute(StartFillAuthorsMetadataCommand.builder().override(false).lang(lang).runId(runId).build());
				break;
			case REVIEWS:
				commandBus.execute(StartFillReviewsMetadataCommand.builder().override(false).lang(lang).runId(runId).build());
				break;
			default:
				break;
			}
		}

	}

	private StartFillBooksMetadataCommand bookCommand(final BookMetadataScope scope, final long runId) {
		return StartFillBooksMetadataCommand.builder()
				.scope(scope)
				.mergePolicy(MetadataMergePolicy.FILL_MISSING)
				.dynamicPolicy(DynamicMetadataPolicy.REFRESH_IF_STALE)
				.runId(runId)
				.build();
	}

}
