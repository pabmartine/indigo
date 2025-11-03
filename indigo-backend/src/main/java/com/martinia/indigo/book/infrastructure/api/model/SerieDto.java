package com.martinia.indigo.book.infrastructure.api.model;

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

	private int index;
	private String name;

}
