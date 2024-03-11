package com.martinia.indigo.notification.infrastructure.mongo.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Id;

import lombok.Builder;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "notifications")
public class NotificationMongoEntity implements Serializable {

	private static final long serialVersionUID = -1130735886474712847L;
	
	@Id
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
