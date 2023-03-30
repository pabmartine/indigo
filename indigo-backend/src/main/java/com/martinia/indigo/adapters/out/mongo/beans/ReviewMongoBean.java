package com.martinia.indigo.adapters.out.mongo.beans;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewMongoBean implements Serializable {

	private String name;
	private String title;
	private String comment;
	private String rating;
	private String date;

}
