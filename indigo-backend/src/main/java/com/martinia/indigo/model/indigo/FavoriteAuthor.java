package com.martinia.indigo.model.indigo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * The persistent class for the "books_authors_link" database table.
 * 
 */
@Entity
@Table(name = "\"favorite_authors\"")
@NamedQuery(name = "FavoriteAuthor.findAll", query = "SELECT b FROM FavoriteAuthor b")
public class FavoriteAuthor implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name = "\"user\"")
	private int user;

	@Column(name = "\"author\"")
	private int author;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"id\"")
	private int id;

	public FavoriteAuthor() {
	}

	public FavoriteAuthor(int user, int author) {
		super();
		this.user = user;
		this.author = author;
	}

	public int getUser() {
		return user;
	}

	public void setUser(int user) {
		this.user = user;
	}

	public int getAuthor() {
		return author;
	}

	public void setAuthor(int author) {
		this.author = author;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

}