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
	private final Map<Long, RunState> activeRuns = new LinkedHashMap<>();

	public synchronized long start(String type, String entity) {
		stop(entity);
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
		activeRuns.put(generation.get(), new RunState(type, entity));
		storeSnapshot();
		return generation.get();
	}

	public synchronized void stop() {
		for (Long runId : java.util.List.copyOf(activeRuns.keySet())) {
			complete(runId);
		}
		this.running = false;
		this.completedAt = System.currentTimeMillis();
		storeSnapshot();
	}

	public synchronized void stop(String entity) {
		for (Long runId : java.util.List.copyOf(activeRuns.keySet())) {
			if (activeRuns.get(runId).entity.equals(entity)) {
				complete(runId);
			}
		}
	}

	public synchronized boolean isRunning() {
		return running || !activeRuns.isEmpty();
	}

	public synchronized void setTotal(long total) {
		this.total = total;
		RunState run = activeRuns.get(generation.get());
		if (run != null) run.total = total;
	}

	public synchronized void complete() {
		if (isActive(generation.get())) {
			complete(generation.get());
			return;
		}
		this.running = false;
		this.completedAt = System.currentTimeMillis();
		storeSnapshot();
	}

	public synchronized void complete(final long runId) {
		RunState run = activeRuns.remove(runId);
		if (run != null) {
			run.completedAt = System.currentTimeMillis();
			publish(runId, run);
			if (runId == generation.get()) {
				running = false;
				completedAt = run.completedAt;
			}
		}
	}

	public synchronized boolean isActive(final long runId) {
		return activeRuns.containsKey(runId);
	}

	public long getRunId() {
		return generation.get();
	}

	public synchronized boolean initializeRun(final long runId, final String message, final long total) {
		if (!isActive(runId)) {
			return false;
		}
		RunState run = activeRuns.get(runId);
		run.message = message;
		run.total = total;
		publish(runId, run);
		return true;
	}

	public synchronized void record(final long runId, final MetadataItemResult result) {
		if (!isActive(runId)) {
			return;
		}
		RunState run = activeRuns.get(runId);
		run.current++;
		switch (result) {
		case FOUND -> run.found++;
		case NOT_FOUND -> run.notFound++;
		case SKIPPED -> run.skipped++;
		case ERROR -> run.errors++;
		}
		if (run.current >= run.total) {
			complete(runId);
		}
		else {
			publish(runId, run);
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
		Map<String, Map<String, Object>> snapshots = new LinkedHashMap<>();
		runs.forEach((key, value) -> snapshots.put(key, new LinkedHashMap<>(value)));
		return snapshots;
	}

	private void publish(long runId, RunState run) {
		storeRunSnapshot(runId, run);
		if (runId == generation.get()) {
			running = activeRuns.containsKey(runId);
			message = run.message;
			total = run.total;
			current = run.current;
			found = run.found;
			notFound = run.notFound;
			skipped = run.skipped;
			errors = run.errors;
			completedAt = run.completedAt;
		}
	}

	private void storeRunSnapshot(long runId, RunState run) {
		Map<String, Object> snapshot = new LinkedHashMap<>();
		snapshot.put("type", run.type);
		snapshot.put("entity", run.entity);
		snapshot.put("status", activeRuns.containsKey(runId));
		snapshot.put("message", run.message);
		snapshot.put("total", run.total);
		snapshot.put("current", run.current);
		snapshot.put("found", run.found);
		snapshot.put("notFound", run.notFound);
		snapshot.put("skipped", run.skipped);
		snapshot.put("errors", run.errors);
		snapshot.put("completedAt", run.completedAt);
		runs.put(run.type + ':' + run.entity, snapshot);
	}

	private static final class RunState {
		private final String type;
		private final String entity;
		private long total;
		private long current;
		private long found;
		private long notFound;
		private long skipped;
		private long errors;
		private String message;
		private Long completedAt;

		private RunState(String type, String entity) {
			this.type = type;
			this.entity = entity;
		}
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
