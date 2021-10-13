package com.martinia.indigo.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookDto implements Serializable {


	private static final long serialVersionUID = 3010775368721977810L;
	private int id;
	private String title;
	private String authorSort;
	private String pubDate;
	private String lastModified;
	private BigDecimal seriesIndex;
	private String path;
	private float rating;
	private String similar;

}