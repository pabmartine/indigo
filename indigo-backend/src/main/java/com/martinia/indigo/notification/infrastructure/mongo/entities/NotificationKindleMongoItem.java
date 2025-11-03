package com.martinia.indigo.notification.infrastructure.mongo.entities;

import com.martinia.indigo.notification.domain.model.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationKindleMongoItem implements Serializable {

	private String book;
}
