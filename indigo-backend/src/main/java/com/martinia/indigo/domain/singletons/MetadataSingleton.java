package com.martinia.indigo.domain.singletons;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MetadataSingleton {

	private String type;
	private boolean running;
	long total = 0;
	long current = 0;
	private String message;

	public void start(String type) {
		this.type = type;
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