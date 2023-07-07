package com.martinia.indigo.book.infrastructure.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SerieDto implements Serializable {

	private static final long serialVersionUID = 7556669089558736925L;
	private int index;
	private String name;

}
