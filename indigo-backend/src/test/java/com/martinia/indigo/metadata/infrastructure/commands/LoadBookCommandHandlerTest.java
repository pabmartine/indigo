package com.martinia.indigo.metadata.infrastructure.commands;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.metadata.domain.model.commands.LoadBookCommand;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.LoadBookUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class LoadBookCommandHandlerTest extends BaseIndigoTest {

	@MockBean
	private LoadBookUseCase loadBookUseCase;

	@Resource
	private LoadBookCommandHandler loadBookCommandHandler;

	@Test
	public void testHandle_Successful() throws Exception {
		// Given
		String bookId = "123";
		boolean override = true;

		// When
		loadBookCommandHandler.handle(LoadBookCommand.builder().bookId(bookId).override(override).build());

		// Then
		// Verify the method invocation
		verify(loadBookUseCase, times(1)).load(bookId, override);
	}
}
