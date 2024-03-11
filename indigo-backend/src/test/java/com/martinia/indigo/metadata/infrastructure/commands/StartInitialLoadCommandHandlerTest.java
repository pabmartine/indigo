package com.martinia.indigo.metadata.infrastructure.commands;

import static org.junit.jupiter.api.Assertions.*;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.model.commands.StartInitialLoadCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.StartInitialLoadUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class StartInitialLoadCommandHandlerTest extends BaseIndigoTest {

	@MockBean
	private StartInitialLoadUseCase startInitialLoadUseCase;

	@Resource
	private StartInitialLoadCommandHandler startInitialLoadCommandHandler;

	@Test
	public void testHandle_Successful() {
		// Given
		boolean override = true;

		// When
		startInitialLoadCommandHandler.handle(StartInitialLoadCommand.builder().override(override).build());

		// Then
		// Verify the method invocation
		verify(startInitialLoadUseCase, times(1)).start(override);
	}
}
