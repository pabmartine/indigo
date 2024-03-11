package com.martinia.indigo.common.bus.command.domain.ports;

import com.martinia.indigo.common.bus.command.domain.model.Command;

import java.util.concurrent.CompletableFuture;

public interface CommandBus {

	<R> CompletableFuture<R> execute(Command<R> command);

	<R> R executeAndWait(Command<R> command);

}