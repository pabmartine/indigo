package com.martinia.indigo.common.domain.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NumBooks implements Serializable {

	private int total = 1;
	private Map<String, Integer> languages = new HashMap<>();

}
