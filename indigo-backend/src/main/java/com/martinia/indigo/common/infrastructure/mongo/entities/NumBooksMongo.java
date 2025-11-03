package com.martinia.indigo.common.infrastructure.mongo.entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NumBooksMongo implements Serializable {

	private int total = 1;
	private Map<String, Integer> languages = new HashMap<>();

}
