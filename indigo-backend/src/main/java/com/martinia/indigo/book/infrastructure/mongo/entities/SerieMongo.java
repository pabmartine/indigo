package com.martinia.indigo.book.infrastructure.mongo.entities;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SerieMongo implements Serializable {

	private static final long serialVersionUID = 7556669089558736925L;
	private int index;
	private String name;

}
