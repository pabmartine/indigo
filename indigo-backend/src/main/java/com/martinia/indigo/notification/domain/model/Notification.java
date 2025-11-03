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
public class Notification implements Serializable {

	private String id;
	private NotificationEnum type;
	private String user;
	private Date date;
	private boolean readUser;
	private boolean readAdmin;
	private StatusEnum status;
	private String message;
	private NotificationEpubFileUploadItem upload;
	private NotificationKindleItem kindle;

}
