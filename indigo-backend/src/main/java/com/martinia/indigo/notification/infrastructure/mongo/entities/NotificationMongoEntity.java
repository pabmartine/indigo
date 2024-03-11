package com.martinia.indigo.notification.infrastructure.mongo.entities;

import com.martinia.indigo.notification.domain.model.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "notifications2")
public class NotificationMongoEntity implements Serializable {

	@Id
	private String id;
	private String type;
	private String user;
	private Date date;
	private boolean readUser;
	private boolean readAdmin;
	private String status;
	private String message;
	private NotificationEpubFileUploadMongoItem upload;
	private NotificationKindleMongoItem kindle;

}
