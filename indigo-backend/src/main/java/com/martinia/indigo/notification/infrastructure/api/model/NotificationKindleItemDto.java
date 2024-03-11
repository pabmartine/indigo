package com.martinia.indigo.notification.infrastructure.api.model;

import com.martinia.indigo.notification.domain.model.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationKindleItemDto implements Serializable {

	private String book;

}