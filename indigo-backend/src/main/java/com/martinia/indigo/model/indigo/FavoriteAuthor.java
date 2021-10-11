package com.martinia.indigo.model.indigo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the "books_authors_link" database table.
 * 
 */
@Getter
@Setter
@NoArgsConstructor
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

	public FavoriteAuthor(int user, int author) {
		super();
		this.user = user;
		this.author = author;
	}

}