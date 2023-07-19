package com.martinia.indigo.common.bus.command;

import com.martinia.indigo.common.bus.command.domain.model.CommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TestCommandHandler implements CommandHandler<TestCommand, String> {

	@Override
	public String handle(final TestCommand command) throws Exception {
		return command.getId();
	}
}
