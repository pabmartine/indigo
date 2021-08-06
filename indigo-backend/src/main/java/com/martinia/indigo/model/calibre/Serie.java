package com.martinia.indigo.model.calibre;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the "series" database table.
 * 
 */
@Entity
@Table(name="\"series\"")
@NamedQuery(name="Serie.findAll", query="SELECT s FROM Serie s")
public class Serie implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="\"id\"")
	private int id;

	@Column(name="\"name\"")
	private String name;

	@Column(name="\"sort\"")
	private String sort;

	public Serie() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
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