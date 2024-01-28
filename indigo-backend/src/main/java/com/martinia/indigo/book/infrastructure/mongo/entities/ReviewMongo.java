package com.martinia.indigo.book.infrastructure.mongo.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewMongo implements Serializable {

	private String name;
	private String title;
	private String comment;
	private int rating;
	private Date date;
	private Date lastMetadataSync;

	private String provider;

}
