package com.martinia.indigo.notification.domain.model;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification implements Serializable {

	private String id;
	private String type;
	private String user;
	private String book;
	private String status;
	private String error;
	private boolean readUser;
	private boolean readAdmin;
	private Date sendDate;
}
