package com.martinia.indigo.model.indigo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * The persistent class for the "authors" database table.
 * 
 */
@Entity
@Table(name = "\"authors\"")
@NamedQuery(name = "MyAuthor.findAll", query = "SELECT a FROM MyAuthor a")
public class MyAuthor implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "\"id\"")
	private int id;

	@Column(name = "\"name\"")
	private String name;

	@Column(name = "\"sort\"")
	private String sort;

	@Column(name = "\"description\"")
	private String description;

	@Column(name = "\"provider\"")
	private String provider;

	@Column(name = "\"image\"")
	private String image;

	public MyAuthor() {
	}

	public MyAuthor(String name, String description, String image, String provider) {
		super();
		this.name = name;
		this.description = description;
		this.image = image;
		this.provider = provider;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}