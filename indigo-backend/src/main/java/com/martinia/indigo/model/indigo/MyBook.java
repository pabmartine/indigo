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
@Table(name = "\"books\"")
@NamedQuery(name = "MyBook.findAll", query = "SELECT a FROM MyBook a")
public class MyBook implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "\"id\"")
	private int id;

	@Column(name = "\"rating\"")
	private float rating;

	@Column(name = "\"similar\"")
	private String similar;

	@Column(name = "\"provider\"")
	private String provider;

	public MyBook() {
	}

	public MyBook(float rating, String similar, String provider) {
		super();
		this.rating = rating;
		this.similar = similar;
		this.provider = provider;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public float getRating() {
		return rating;
	}

	public void setRating(float rating) {
		this.rating = rating;
	}

	public String getSimilar() {
		return similar;
	}

	public void setSimilar(String similar) {
		this.similar = similar;
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