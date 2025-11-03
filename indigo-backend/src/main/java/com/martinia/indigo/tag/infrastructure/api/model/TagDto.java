package com.martinia.indigo.tag.infrastructure.api.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagDto implements Serializable {

	private String id;
	private String name;
	private String image;
	private int numBooks;
}
