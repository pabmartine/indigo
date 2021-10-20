package com.martinia.indigo.adapters.out.mongo.entities;

import java.io.Serializable;

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
@Document(collection = "tags")
public class TagMongoEntity implements Serializable {


	private static final long serialVersionUID = 3821856920529740875L;

	@Id
	private String id;
	private String name;
	private String image;
	private int numBooks;
	
	public TagMongoEntity(String tag) {
		this.name = tag;
		this.numBooks = 1;
	}
}
