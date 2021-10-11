package com.martinia.indigo.model.calibre;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the "books" database table.
 * 
 */
@Getter
@Setter
@NoArgsConstructor
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

}