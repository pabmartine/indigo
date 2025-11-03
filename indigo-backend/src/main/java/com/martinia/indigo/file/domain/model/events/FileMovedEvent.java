package com.martinia.indigo.file.domain.model.events;

import com.martinia.indigo.common.bus.event.domain.model.Event;
import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

@Data
@Builder
public class FileMovedEvent implements Event {

	private final Path sourcePath;

}