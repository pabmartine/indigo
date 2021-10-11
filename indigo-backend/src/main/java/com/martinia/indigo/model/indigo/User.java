package com.martinia.indigo.model.indigo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "\"users\"")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"id\"")
	private int id;

	@Column(name = "\"username\"")
	private String username;

	@Column(name = "\"password\"")
	private String password;

	@Column(name = "\"kindle\"")
	private String kindle;

	@Column(name = "\"role\"")
	private String role;

	@Column(name = "\"language\"")
	private String language;

	@Type(type = "numeric_boolean")
	@Column(name = "\"show_random_books\"")
	private boolean showRandomBooks;

	public User(String username, String password, String kindle, String role, String language) {
		super();
		this.username = username;
		this.password = password;
		this.kindle = kindle;
		this.role = role;
		this.language = language;
	}

}
