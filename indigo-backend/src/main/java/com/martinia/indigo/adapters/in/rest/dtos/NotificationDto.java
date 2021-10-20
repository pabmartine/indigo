package com.martinia.indigo.adapters.in.rest.dtos;

import java.io.Serializable;

import com.martinia.indigo.domain.enums.NotificationEnum;
import com.martinia.indigo.domain.enums.StatusEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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