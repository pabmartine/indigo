package com.martinia.indigo.metadata.domain.model.commands;

import com.martinia.indigo.common.bus.command.domain.model.Command;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindReviewMetadataCommand extends Command<MetadataItemResult> {
	private boolean override;
	private String bookId;
	private String lang;
	private long lastExecution;
}
