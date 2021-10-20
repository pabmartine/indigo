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
 * The persistent class for the "books_languages_link" database table.
 * 
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "\"books_languages_link\"")
public class BooksLanguagesLinkSqliteEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name = "\"book\"")
	private int book;

	@Id
	@Column(name = "\"id\"")
	private int id;

	@Column(name = "\"item_order\"")
	private int itemOrder;

	@Column(name = "\"lang_code\"")
	private int langCode;

}