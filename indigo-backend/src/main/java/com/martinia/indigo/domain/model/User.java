package com.martinia.indigo.domain.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

	private static final long serialVersionUID = 9011428722737102324L;

	private String id;
	private String username;
	private String password;
	private String kindle;
	private String role;
	private String language;
	private List<String> languageBooks;
	private List<String> favoriteBooks;
	private List<String> favoriteAuthors;

	public User(String username, String password, String role, String language) {
		this.username = username;
		this.password = password;
		this.role = role;
		this.language = language;
	}

}
