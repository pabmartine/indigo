package com.martinia.indigo.domain.singletons;

public class MetadataSingleton {

	private String type;
	private boolean running;
	long total = 0;
	long current = 0;

	public MetadataSingleton() {

	}
	
	public void start(String type) {
		this.type = type;
		this.running = true;
	}
	
	public void stop() {
		this.type = null;
		this.running = false;
		this.total = 0;
		this.current = 0;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public long getCurrent() {
		return current;
	}

	public void setCurrent(long current) {
		this.current = current;
	}

}
