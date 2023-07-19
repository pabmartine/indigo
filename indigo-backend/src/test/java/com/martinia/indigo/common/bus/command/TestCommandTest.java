package com.martinia.indigo.common.bus.command;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

public class TestCommandTest extends BaseIndigoTest {

	@Resource
	private CommandBus commandBus;

	@Test
	public void testCommandAsync() throws Exception {
		//Given
		String input = "Hello world";
		//When
		String result = commandBus.execute(TestCommand.builder().id(input).build()).get();
		//Then
		Assertions.assertEquals(result, input);
	}

	@Test
	public void testCommandSync() throws Exception {
		//Given
		String input = "Hello world";
		//When
		String result = commandBus.executeAndWait(TestCommand.builder().id(input).build());
		//Then
		Assertions.assertEquals(result, input);
	}

}
