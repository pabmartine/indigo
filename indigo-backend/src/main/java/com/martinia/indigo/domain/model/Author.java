package com.martinia.indigo.domain.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Author implements Serializable {

	private static final long serialVersionUID = 6946290843722836107L;

	private String id;
	private String name;
	private String sort;
	private String description;
	private String provider;
	private String image;
	private int numBooks;

}
