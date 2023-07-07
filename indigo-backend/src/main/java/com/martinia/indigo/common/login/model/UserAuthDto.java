package com.martinia.indigo.common.login.model;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAuthDto implements Authentication {

	private static final long serialVersionUID = -7861422165632063184L;
	private String name;
	private boolean authenticated = true;

	public UserAuthDto(String name) {
		super();
		this.name = name;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return null;
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getDetails() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return null;
	}

}
