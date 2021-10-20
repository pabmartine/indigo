package com.martinia.indigo.domain.model.inner;

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

	private static final long serialVersionUID = 7556669089558736925L;
	private int index;
	private String name;

}
