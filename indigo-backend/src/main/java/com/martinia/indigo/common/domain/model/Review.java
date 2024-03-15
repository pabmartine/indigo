package com.martinia.indigo.common.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
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