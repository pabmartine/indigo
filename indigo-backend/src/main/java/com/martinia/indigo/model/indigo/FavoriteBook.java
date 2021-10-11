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
@Table(name = "\"favorite_books\"")
@NamedQuery(name = "FavoriteBook.findAll", query = "SELECT b FROM FavoriteBook b")
public class FavoriteBook implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name = "\"user\"")
	private int user;

	@Column(name = "\"book\"")
	private int book;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "\"id\"")
	private int id;

	public FavoriteBook(int user, int book) {
		super();
		this.user = user;
		this.book = book;
	}

}