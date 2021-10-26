package com.martinia.indigo.domain.model;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class View implements Serializable {

	private static final long serialVersionUID = -4944609485920613732L;
	private String id;
	private String user;
	private String book;
	private Date viewDate;

	public View(String book, String user) {
		this.book = book;
		this.user = user;
		this.viewDate = new Date();
	}
}
