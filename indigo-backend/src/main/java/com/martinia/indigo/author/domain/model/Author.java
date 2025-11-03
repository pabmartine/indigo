package com.martinia.indigo.author.domain.model;

import com.martinia.indigo.common.domain.model.NumBooks;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Author implements Serializable {

	private String id;
	private String name;
	private String sort;
	private String description;
	private String provider;
	private String image;
	private NumBooks numBooks;
	private Date lastMetadataSync;

}
