package com.martinia.indigo.user.infrastructure.mongo.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
	private List<String> languageBooks = new ArrayList<>();
	private List<String> favoriteBooks = new ArrayList<>();
	private List<String> favoriteAuthors = new ArrayList<>();

}
