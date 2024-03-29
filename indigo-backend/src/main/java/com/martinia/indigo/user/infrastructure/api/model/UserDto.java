package com.martinia.indigo.user.infrastructure.api.model;

import java.io.Serializable;
import java.util.List;

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
	private String id;
	private String username;
	private String password;
	private String kindle;
	private String role;
	private String language;
	private List<String> languageBooks;

}
