package com.martinia.indigo.common.bus.event;

import com.martinia.indigo.common.bus.event.domain.model.Event;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestEvent implements Event {
	private String id;
}
