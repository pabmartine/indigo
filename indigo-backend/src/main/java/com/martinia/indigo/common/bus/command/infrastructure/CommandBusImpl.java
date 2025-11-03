package com.martinia.indigo.common.bus.command.infrastructure;

import com.martinia.indigo.common.bus.command.domain.model.Command;
import com.martinia.indigo.common.bus.command.domain.model.CommandHandler;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Component
@Primary
@Slf4j
public class CommandBusImpl implements CommandBus {

	private Map<Class, CommandHandler> handlers;

	public CommandBusImpl() {
		this.handlers = new HashMap<>();
	}

	@Autowired
	public void setCommandHandlerImplementations(List<CommandHandler> commandHandlerImplementations) {
		commandHandlerImplementations.forEach(commandHandler -> {
			final Class<?> commandClass = commandHandler.getCommandClass();
			Optional.ofNullable(commandClass).ifPresent(clazz -> handlers.put(clazz, commandHandler));
		});
	}

	@Override
	public <R> CompletableFuture<R> execute(final Command<R> command) {
		if (!handlers.containsKey(command.getClass())) {
			throw new RuntimeException(String.format("No handler for %s", command.getClass().getName()));
		}
		CommandHandler handler = handlers.get(command.getClass());
		return (CompletableFuture<R>) CompletableFuture.supplyAsync(() -> {
			try {
				return handler.handle(command);
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
		}).handle((result, ex) -> {
			if (ex != null) {
				log.error(ex.getMessage());
				throw new CompletionException(ex);
			}

			return result;
		});
	}

	@Override
	public <R> R executeAndWait(final Command<R> command) {
		try {
			return execute(command).join();
		}
		catch (CancellationException | CompletionException ex) {
			if (ex.getCause() instanceof RuntimeException) {
				throw (RuntimeException) ex.getCause();
			}
			throw new RuntimeException("Error executing command", ex);
		}
	}

}