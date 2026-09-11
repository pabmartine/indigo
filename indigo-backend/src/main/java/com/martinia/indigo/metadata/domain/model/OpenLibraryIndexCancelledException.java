package com.martinia.indigo.metadata.domain.model;

public class OpenLibraryIndexCancelledException extends RuntimeException {
	public OpenLibraryIndexCancelledException() {
		super("Open Library index update was cancelled");
	}
}
