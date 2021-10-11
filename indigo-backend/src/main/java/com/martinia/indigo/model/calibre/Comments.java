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
@Table(name = "\"comments\"")
@NamedQuery(name = "Comments.findAll", query = "SELECT c FROM Comments c")
public class Comments implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "\"id\"")
	private int id;

	@Column(name = "\"book\"")
	private int book;

	@Column(name = "\"text\"")
	private String text;

}