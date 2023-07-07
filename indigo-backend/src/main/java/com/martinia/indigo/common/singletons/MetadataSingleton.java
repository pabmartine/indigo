package com.martinia.indigo.common.singletons;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MetadataSingleton {

	private String type;
	private String entity;
	private boolean running;
	long total = 0;
	long current = 0;
	private String message;

	public void start(String type, String entity) {
		this.type = type;
		this.entity = entity;
		this.running = true;
	}

	public void stop() {
		this.type = null;
		this.running = false;
		this.total = 0;
		this.current = 0;
		this.message = null;
	}

}
