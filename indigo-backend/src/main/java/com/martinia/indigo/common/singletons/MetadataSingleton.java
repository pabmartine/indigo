package com.martinia.indigo.common.singletons;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.martinia.indigo.metadata.domain.model.MetadataItemResult;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@Setter
@NoArgsConstructor
public class MetadataSingleton {

	private volatile String type;
	private volatile String entity;
	private volatile boolean running;
	volatile long total = 0;
	volatile long current = 0;
	private volatile long found;
	private volatile long notFound;
	private volatile long skipped;
	private volatile long errors;
	private volatile String message;
	private volatile Long completedAt;
	private final AtomicLong generation = new AtomicLong();
	private final Map<String, Map<String, Object>> runs = new LinkedHashMap<>();

	public synchronized long start(String type, String entity) {
		generation.incrementAndGet();
		this.type = type;
		this.entity = entity;
		this.total = 0;
		this.current = 0;
		this.found = 0;
		this.notFound = 0;
		this.skipped = 0;
		this.errors = 0;
		this.message = null;
		this.completedAt = null;
		this.running = true;
		storeSnapshot();
		return generation.get();
	}

	public synchronized void stop() {
		generation.incrementAndGet();
		this.running = false;
		this.completedAt = System.currentTimeMillis();
		storeSnapshot();
	}

	public synchronized void complete() {
		this.running = false;
		this.completedAt = System.currentTimeMillis();
		storeSnapshot();
	}

	public synchronized void complete(final long runId) {
		if (isActive(runId)) {
			complete();
		}
	}

	public boolean isActive(final long runId) {
		return running && generation.get() == runId;
	}

	public long getRunId() {
		return generation.get();
	}

	public synchronized boolean initializeRun(final long runId, final String message, final long total) {
		if (!isActive(runId)) {
			return false;
		}
		this.message = message;
		this.total = total;
		storeSnapshot();
		return true;
	}

	public synchronized void record(final long runId, final MetadataItemResult result) {
		if (!isActive(runId)) {
			return;
		}
		current++;
		switch (result) {
		case FOUND -> found++;
		case NOT_FOUND -> notFound++;
		case SKIPPED -> skipped++;
		case ERROR -> errors++;
		}
		if (current >= total) {
			complete();
		}
		else {
			storeSnapshot();
		}
	}

	public synchronized void increase() {
		if (running) {
			this.current++;
			if (this.current == this.total) {
				complete();
			}
		}
	}

	public synchronized Map<String, Map<String, Object>> getRuns() {
		return new LinkedHashMap<>(runs);
	}

	private void storeSnapshot() {
		if (type == null || entity == null) {
			return;
		}
		final Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("type", type);
		snapshot.put("entity", entity);
		snapshot.put("status", running);
		snapshot.put("total", total);
		snapshot.put("current", current);
		snapshot.put("found", found);
		snapshot.put("notFound", notFound);
		snapshot.put("skipped", skipped);
		snapshot.put("errors", errors);
		snapshot.put("completedAt", completedAt);
		runs.put(type + ':' + entity, snapshot);
	}
}
