package com.martinia.indigo.common.exceptions;

public class MetadataRunningException extends RuntimeException {

	public MetadataRunningException() {
		super("Action cannot be performed while the metadata service is running.");
	}
}
