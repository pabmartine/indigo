package com.martinia.indigo.model.indigo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
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

	public MyAuthor(String name, String description, String image, String provider) {
		super();
		this.name = name;
		this.description = description;
		this.image = image;
		this.provider = provider;
	}

}