package com.martinia.indigo.file.domain.model.commands;

import com.martinia.indigo.common.bus.command.domain.model.Command;
import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

@Data
@Builder
public class ExtractEpubFileCommand extends Command<Void> {
	private Path file;
}
