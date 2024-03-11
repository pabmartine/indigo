package com.martinia.indigo.metadata.domain.model.commands;

import com.martinia.indigo.common.bus.command.domain.model.Command;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindBookMetadataCommand extends Command<Void> {
	private boolean override;
	private String bookId;
	private long lastExecution;
}
