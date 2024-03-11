package com.martinia.indigo.common.bus.command;

import com.martinia.indigo.common.bus.command.domain.model.Command;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestCommand extends Command<String> {
	private String id;

	boolean exception;
}
