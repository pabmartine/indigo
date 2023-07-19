package com.martinia.indigo.metadata.domain.ports.commands;

import com.martinia.indigo.common.bus.command.domain.model.Command;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoadBookCommand extends Command<Void> {
	private String bookId;
}
