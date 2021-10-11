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
 * The persistent class for the "tags" database table.
 * 
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "\"tags\"")
@NamedQuery(name = "Tag.findAll", query = "SELECT t FROM Tag t")
public class Tag implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "\"id\"")
	private int id;

	@Column(name = "\"name\"")
	private String name;

}