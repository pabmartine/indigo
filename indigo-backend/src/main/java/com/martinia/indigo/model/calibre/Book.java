package com.martinia.indigo.model.calibre;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * The persistent class for the "books" database table.
 * 
 */
@Entity
@Table(name = "\"books\"")
@NamedQuery(name = "Book.findAll", query = "SELECT b FROM Book b")
public class Book implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name = "\"author_sort\"")
	private String authorSort;

	@Id
	@Column(name = "\"id\"")
	private int id;

	@Column(name = "\"last_modified\"")
	private String lastModified;

	@Column(name = "\"path\"")
	private String path;

	@Column(name = "\"pubdate\"")
	private String pubdate;

	@Column(name = "\"series_index\"")
	private BigDecimal seriesIndex;

	@Column(name = "\"title\"")
	private String title;

	public Book() {
	}

	public String getAuthorSort() {
		return this.authorSort;
	}

	public void setAuthorSort(String authorSort) {
		this.authorSort = authorSort;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLastModified() {
		return this.lastModified;
	}

	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPubdate() {
		return this.pubdate;
	}

	public void setPubdate(String pubdate) {
		this.pubdate = pubdate;
	}

	public BigDecimal getSeriesIndex() {
		return this.seriesIndex;
	}

	public void setSeriesIndex(BigDecimal seriesIndex) {
		this.seriesIndex = seriesIndex;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}



}