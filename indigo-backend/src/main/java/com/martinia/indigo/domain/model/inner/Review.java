package com.martinia.indigo.domain.model.inner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@AllArgsConstructor
@Data
@Builder
public class Review {
	private String name;
	private String title;
	private String comment;
	private int rating;
	private Date date;
	private Date lastMetadataSync;

	private String provider;

}