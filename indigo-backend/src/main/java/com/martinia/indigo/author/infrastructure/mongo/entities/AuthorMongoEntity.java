package com.martinia.indigo.author.infrastructure.mongo.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "authors")
public class AuthorMongoEntity implements Serializable {

	private static final long serialVersionUID = 6946290843722836107L;

	@Id
	private String id;
	private String name;
	private String sort;
	private String description;
	private String provider;
	private String image;
	private NumBooksMongo numBooks;

	private Date lastMetadataSync;

}
