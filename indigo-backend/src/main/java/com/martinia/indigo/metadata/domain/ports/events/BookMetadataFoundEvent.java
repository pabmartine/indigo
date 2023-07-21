package com.martinia.indigo.metadata.domain.ports.events;

import com.martinia.indigo.common.bus.event.domain.model.Event;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookMetadataFoundEvent implements Event {

	private String bookId;
	private String similar;
}
