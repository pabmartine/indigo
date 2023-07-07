package com.martinia.indigo.author.domain.model;

import java.io.Serializable;
import java.util.Date;

import com.martinia.indigo.common.model.NumBooks;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
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
	private NumBooks numBooks;
	private Date lastMetadataSync;

}
