package com.martinia.indigo.model.calibre;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the "books_languages_link" database table.
 * 
 */
@Entity
@Table(name="\"books_languages_link\"")
@NamedQuery(name="BooksLanguagesLink.findAll", query="SELECT b FROM BooksLanguagesLink b")
public class BooksLanguagesLink implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name = "\"book\"")
	private int book;

	@Id
	@Column(name="\"id\"")
	private int id;

	@Column(name="\"item_order\"")
	private int itemOrder;

	@Column(name="\"lang_code\"")
	private int langCode;

	public BooksLanguagesLink() {
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

	public int getItemOrder() {
		return this.itemOrder;
	}

	public void setItemOrder(int itemOrder) {
		this.itemOrder = itemOrder;
	}

	public int getLangCode() {
		return this.langCode;
	}

	public void setLangCode(int langCode) {
		this.langCode = langCode;
	}

}