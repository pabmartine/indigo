package com.martinia.indigo.serie.domain.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Serie implements Serializable {

	private static final long serialVersionUID = -4035270018216666984L;
	
	private String id;
	private String name;
	private int numBooks;

}
