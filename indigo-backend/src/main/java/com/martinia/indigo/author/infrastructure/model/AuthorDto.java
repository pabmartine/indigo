package com.martinia.indigo.author.infrastructure.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthorDto implements Serializable {

	private static final long serialVersionUID = -342588061916435330L;

	private String id;
	private String name;
	private String sort;
	private String description;
	private String provider;
	private String image;
	private int numBooks;

}