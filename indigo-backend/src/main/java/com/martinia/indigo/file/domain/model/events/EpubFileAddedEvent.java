package com.martinia.indigo.file.domain.model.events;

import com.martinia.indigo.common.bus.event.domain.model.Event;
import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

@Data
@Builder
public class EpubFileAddedEvent implements Event {

	private String bookId;
	private Path sourcePath;
	private Path targetPath;
	private String authorImage;

}