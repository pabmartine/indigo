package com.martinia.indigo.notification.infrastructure.mongo.entities;

import com.martinia.indigo.notification.domain.model.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "notifications")
@CompoundIndexes({
		@CompoundIndex(name = "user_type_idx", def = "{'user': 1, 'type': 1}"),
		@CompoundIndex(name = "user_readUser_idx", def = "{'user': 1, 'readUser': 1}")
})
public class NotificationMongoEntity implements Serializable {

	@Id
	private String id;
	@Indexed
	private String type;
	@Indexed
	private String user;
	@Indexed
	private Date date;
	private boolean readUser;
	@Indexed
	private boolean readAdmin;
	private String status;
	private String message;
	private NotificationEpubFileUploadMongoItem upload;
	private NotificationKindleMongoItem kindle;

}
