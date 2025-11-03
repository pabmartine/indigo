package com.martinia.indigo.file.domain.model.events;

import com.martinia.indigo.notification.domain.model.events.NotificationEvent;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class UploadEpubFilesProcessFinishedEvent extends NotificationEvent {

	private long total;
	private long extract;
	private long extractError;
	private long moveError;
	private long deleteError;
	private long newBooks;
	private long updatedBooks;
	private long newAuthors;
	private long newTags;
	private long moved;
	private long deleted;

}