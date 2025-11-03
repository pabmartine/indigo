package com.martinia.indigo.file.domain.model.events;

import com.martinia.indigo.notification.domain.model.StatusEnum;
import com.martinia.indigo.notification.domain.model.events.NotificationEvent;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class EmailSendedEvent extends NotificationEvent {
	private String book;
}