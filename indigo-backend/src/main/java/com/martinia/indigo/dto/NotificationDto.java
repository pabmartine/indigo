package com.martinia.indigo.dto;

import java.io.Serializable;

import com.martinia.indigo.enums.NotificationEnum;
import com.martinia.indigo.enums.StatusEnum;

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
	private int id;
	private NotificationEnum type;
	private int user;
	private int book;
	private StatusEnum status;
	private String error;
	private boolean readByUser;
	private boolean readByAdmin;
	private String sendDate;
}