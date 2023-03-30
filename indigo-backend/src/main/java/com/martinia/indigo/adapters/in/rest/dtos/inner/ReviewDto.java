package com.martinia.indigo.adapters.in.rest.dtos.inner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class ReviewDto {
	private String name;
	private String title;
	private String comment;
	private String rating;
	private String date;

}