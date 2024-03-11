package com.martinia.indigo.tag.domain.model;

import java.io.Serializable;

import com.martinia.indigo.common.model.NumBooks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Tag implements Serializable {

	private String id;
	private String name;
	private String image;
	private NumBooks numBooks;

}
