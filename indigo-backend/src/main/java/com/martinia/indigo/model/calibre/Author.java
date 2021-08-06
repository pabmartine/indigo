package com.martinia.indigo.model.calibre;

import java.io.Serializable;
import javax.persistence.*;



/**
 * The persistent class for the "authors" database table.
 * 
 */
@Entity
@Table(name="\"authors\"")
@NamedQuery(name="Author.findAll", query="SELECT a FROM Author a")
public class Author implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="\"id\"")
	private int id;

	@Column(name="\"link\"")
	private String link;

	@Column(name="\"name\"")
	private String name;

	@Column(name="\"sort\"")
	private String sort;

	public Author() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLink() {
		return this.link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSort() {
		return this.sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

}