package com.martinia.indigo.adapters.out.mongo.beans;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewMongoBean implements Serializable {

	private String name;
	private String title;
	private String comment;
	private int rating;
	private Date date;
	private Date lastMetadataSync;

	private String provider;

}
