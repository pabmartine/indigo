package com.martinia.indigo.model.indigo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "\"configurations\"")
public class Configuration {

	@Id
	@Column(name = "\"key\"")
	private String key;

	@Column(name = "\"value\"")
	private String value;

}
