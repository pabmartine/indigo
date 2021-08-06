package com.martinia.indigo.model.calibre;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the "books_tags_link" database table.
 * 
 */
@Entity
@Table(name="\"books_tags_link\"")
@NamedQuery(name="BooksTagsLink.findAll", query="SELECT b FROM BooksTagsLink b")
public class BooksTagsLink implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name="\"book\"")
	private int book;

	@Id
	@Column(name="\"id\"")
	private int id;

	@Column(name="\"tag\"")
	private int tag;

	public BooksTagsLink() {
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

	public int getTag() {
		return this.tag;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}

}