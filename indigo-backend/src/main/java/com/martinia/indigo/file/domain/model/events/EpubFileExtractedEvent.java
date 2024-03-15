package com.martinia.indigo.file.domain.model.events;

import com.martinia.indigo.common.bus.event.domain.model.Event;
import com.martinia.indigo.common.domain.model.BookOpf;
import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

@Data
@Builder
public class EpubFileExtractedEvent implements Event {

	final Path path;
	final BookOpf bookOpf;

}