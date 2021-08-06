package com.martinia.indigo.model.calibre;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the "books_authors_link" database table.
 * 
 */
@Entity
@Table(name="\"books_authors_link\"")
@NamedQuery(name="BooksAuthorsLink.findAll", query="SELECT b FROM BooksAuthorsLink b")
public class BooksAuthorsLink implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name="\"author\"")
	private int author;

	@Column(name="\"book\"")
	private int book;

	@Id
	@Column(name="\"id\"")
	private int id;

	public BooksAuthorsLink() {
	}

	public int getAuthor() {
		return this.author;
	}

	public void setAuthor(int author) {
		this.author = author;
	}

	public int getBook() {
		return this.book;
	}

	public void setBook(int book) {
		this.book = book;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

}