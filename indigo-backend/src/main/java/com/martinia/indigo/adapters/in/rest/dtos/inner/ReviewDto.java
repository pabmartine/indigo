package com.martinia.indigo.adapters.in.rest.dtos.inner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@AllArgsConstructor
@Data
@Builder
public class ReviewDto {
	private String name;
	private String title;
	private String comment;
	private int rating;
	private Date date;

}