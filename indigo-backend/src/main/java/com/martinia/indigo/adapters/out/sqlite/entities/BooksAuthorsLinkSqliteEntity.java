package com.martinia.indigo.adapters.out.sqlite.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
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
@Table(name = "\"books_authors_link\"")
public class BooksAuthorsLinkSqliteEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name = "\"author\"")
	private int author;

	@Column(name = "\"book\"")
	private int book;

	@Id
	@Column(name = "\"id\"")
	private int id;

}