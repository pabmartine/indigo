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
@Table(name = "\"tags\"")
@NamedQuery(name = "MyTag.findAll", query = "SELECT a FROM MyTag a")
public class MyTag implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "\"id\"")
	private int id;

	@Column(name = "\"image\"")
	private String image;

}