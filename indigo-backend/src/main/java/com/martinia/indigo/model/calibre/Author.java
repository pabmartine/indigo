package com.martinia.indigo.model.calibre;

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
 * The persistent class for the "authors" database table.
 * 
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "\"authors\"")
@NamedQuery(name = "Author.findAll", query = "SELECT a FROM Author a")
public class Author implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "\"id\"")
	private int id;

	@Column(name = "\"link\"")
	private String link;

	@Column(name = "\"name\"")
	private String name;

	@Column(name = "\"sort\"")
	private String sort;

}