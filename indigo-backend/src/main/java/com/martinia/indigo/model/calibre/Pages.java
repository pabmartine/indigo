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
 * The persistent class for the "custom_column_5" database table.
 * 
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "\"custom_column_5\"")
@NamedQuery(name = "Pages.findAll", query = "SELECT c FROM Pages c")
public class Pages implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "\"id\"")
	private int id;

	@Column(name = "\"book\"")
	private int book;

	@Column(name = "\"value\"")
	private int value;

}