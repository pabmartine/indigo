package com.martinia.indigo.file.domain.model.events;

import com.martinia.indigo.common.bus.event.domain.model.Event;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthorAddedEvent implements Event {

	private String authorId;

}