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


	private static final long serialVersionUID = -4795328375068921585L;
	
	@Id
	private String id;
	private String user;
	private String book;
	private Date viewDate;
}
