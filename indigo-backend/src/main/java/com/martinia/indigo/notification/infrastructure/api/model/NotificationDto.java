package com.martinia.indigo.notification.infrastructure.api.model;

import java.io.Serializable;

import com.martinia.indigo.notification.domain.model.NotificationEnum;
import com.martinia.indigo.notification.domain.model.NotificationEpubFileUploadItem;
import com.martinia.indigo.notification.domain.model.StatusEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto implements Serializable {
	
	private String id;
	private String type;
	private String user;
	private String date;
	private boolean readUser;
	private boolean readAdmin;
	private StatusEnum status;
	private String message;
	private NotificationKindleItemDto kindle;
	private NotificationEpubFileUploadItem upload;

}