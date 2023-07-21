package com.martinia.indigo.metadata.domain.ports.commands;

import com.martinia.indigo.common.bus.command.domain.model.Command;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StartFillAuthorsMetadataCommand extends Command<Void> {
	private boolean override;
	private String lang;
}
