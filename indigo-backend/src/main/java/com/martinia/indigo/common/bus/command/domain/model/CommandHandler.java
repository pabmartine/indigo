package com.martinia.indigo.common.bus.command.domain.model;

public interface CommandHandler<T extends Command<R>, R> {

	R handle(T command) throws Exception;
}