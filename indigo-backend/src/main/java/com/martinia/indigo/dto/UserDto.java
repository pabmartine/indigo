package com.martinia.indigo.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto implements Serializable {

	private static final long serialVersionUID = 4542051545576599212L;
	private int id;
	private String username;
	private String password;
	private String kindle;
	private String role;
	private String language;
	private boolean showRandomBooks;

}
