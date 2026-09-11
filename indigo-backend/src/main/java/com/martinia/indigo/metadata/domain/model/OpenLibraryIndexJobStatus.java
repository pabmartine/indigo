package com.martinia.indigo.metadata.domain.model;

public enum OpenLibraryIndexJobStatus {
	IDLE,
	CHECKING_SPACE,
	DOWNLOADING_EDITIONS,
	PROCESSING_EDITIONS,
	DOWNLOADING_RATINGS,
	PROCESSING_RATINGS,
	ACTIVATING,
	COMPLETED,
	FAILED,
	CANCELLED;

	public boolean isRunning() {
		return this != IDLE && this != COMPLETED && this != FAILED && this != CANCELLED;
	}
}
