package com.martinia.indigo.metadata.domain.ports.usecases.commands;

public interface LoadBookUseCase {

	void load(String bookId, final boolean override);
}
