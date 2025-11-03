package com.martinia.indigo.notification.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationEpubFileUploadItem implements Serializable {

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
