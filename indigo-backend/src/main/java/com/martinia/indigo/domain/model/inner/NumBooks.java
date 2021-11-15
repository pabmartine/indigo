package com.martinia.indigo.domain.model.inner;

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

	private static final long serialVersionUID = 7556669089558736925L;
	private int total = 1;
	private Map<String, Integer> languages = new HashMap<>();

}
