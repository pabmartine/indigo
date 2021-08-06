package com.martinia.indigo.model.indigo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

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


	public User() {
		super();
	}

	public User(String username, String password, String kindle, String role, String language) {
		super();
		this.username = username;
		this.password = password;
		this.kindle = kindle;
		this.role = role;
		this.language = language;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getKindle() {
		return kindle;
	}

	public void setKindle(String kindle) {
		this.kindle = kindle;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}




	public boolean isShowRandomBooks() {
		return showRandomBooks;
	}

	public void setShowRandomBooks(boolean showRandomBooks) {
		this.showRandomBooks = showRandomBooks;
	}


}
