package com.martinia.indigo.notification.domain.model.events;

import com.martinia.indigo.common.bus.event.domain.model.Event;
import com.martinia.indigo.notification.domain.model.NotificationEnum;
import com.martinia.indigo.notification.domain.model.StatusEnum;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Data
@SuperBuilder
public class NotificationEvent implements Event {
	private String id;
	private NotificationEnum type;
	private String user;
	private Date date;
	private boolean readUser;
	private boolean readAdmin;
	private StatusEnum status;
	private String message;
}