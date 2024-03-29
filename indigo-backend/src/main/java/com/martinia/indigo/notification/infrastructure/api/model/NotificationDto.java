package com.martinia.indigo.notification.infrastructure.api.model;

import java.io.Serializable;

import com.martinia.indigo.notification.domain.model.NotificationEnum;
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
	

	private static final long serialVersionUID = 3222962784634993872L;
	private String id;
	private NotificationEnum type;
	private String user;
	private String book;
	private StatusEnum status;
	private String error;
	private boolean readUser;
	private boolean readAdmin;
	private String sendDate;
}