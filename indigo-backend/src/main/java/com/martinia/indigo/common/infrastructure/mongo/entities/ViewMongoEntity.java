package com.martinia.indigo.common.infrastructure.mongo.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "views")
public class ViewMongoEntity implements Serializable {

	@Id
	private String id;
	private String user;
	private String book;
	private Date viewDate;
}
