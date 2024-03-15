package com.martinia.indigo.common.error.infrastructure.model;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Class that will map errors to return in REST response
 * 
 *
 */
@Getter
@AllArgsConstructor
public class ErrorMessageDto implements Serializable {

	private int statusCode;
	private Date timestamp;
	private String message;
	private String description;

}