package com.martinia.indigo.domain.model.inner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class Review {
	private String name;
	private String title;
	private String comment;
	private String rating;
	private String date;

}