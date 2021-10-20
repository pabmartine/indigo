package com.martinia.indigo.adapters.out.mongo.entities;

import java.io.Serializable;
import java.util.List;

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
@Document(collection = "users")
public class UserMongoEntity implements Serializable {

	private static final long serialVersionUID = 9011428722737102324L;

	@Id
	private String id;
	private String username;
	private String password;
	private String kindle;
	private String role;
	private String language;
	private List<String> favoriteBooks;
	private List<String> favoriteAuthors;

}
