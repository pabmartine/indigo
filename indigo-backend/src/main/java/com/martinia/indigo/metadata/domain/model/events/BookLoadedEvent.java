package com.martinia.indigo.metadata.domain.model.events;

import com.martinia.indigo.common.bus.event.domain.model.Event;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookLoadedEvent implements Event {

	private String bookId;

}