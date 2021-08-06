package com.martinia.indigo.model.calibre;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the "books_series_link" database table.
 * 
 */
@Entity
@Table(name="\"books_series_link\"")
@NamedQuery(name="BooksSeriesLink.findAll", query="SELECT b FROM BooksSeriesLink b")
public class BooksSeriesLink implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name="\"book\"")
	private int book;

	@Id
	@Column(name="\"id\"")
	private int id;

	@Column(name="\"series\"")
	private int series;

	public BooksSeriesLink() {
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

	public int getSeries() {
		return this.series;
	}

	public void setSeries(int series) {
		this.series = series;
	}

}