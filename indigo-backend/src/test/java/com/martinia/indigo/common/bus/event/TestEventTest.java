package com.martinia.indigo.common.bus.event;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.common.bus.event.domain.ports.EventBus;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

public class TestEventTest extends BaseIndigoTest {

	@Resource
	private EventBus eventBus;

	@Test
	public void testEvent() {
		//Given
		String input = "Hello world";
		//When
		eventBus.publish(TestEvent.builder().id(input).build());
		//Then
		assertEventPublished(TestEvent.class);
	}

}
