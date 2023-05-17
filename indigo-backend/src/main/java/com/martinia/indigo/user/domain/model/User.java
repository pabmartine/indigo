package com.martinia.indigo.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

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

	public User(String username, String password, String role, String language, List<String> languageBooks) {
		this.username = username;
		this.password = password;
		this.role = role;
		this.language = language;
		this.languageBooks = languageBooks;
	}

}
