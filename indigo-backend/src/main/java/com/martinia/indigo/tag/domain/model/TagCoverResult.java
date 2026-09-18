package com.martinia.indigo.tag.domain.model;

import java.net.URI;

public class TagCoverResult {

	public enum Type {
		EMPTY,
		BYTES,
		REDIRECT
	}

	private final Type type;
	private final byte[] bytes;
	private final URI redirectUri;

	private TagCoverResult(Type type, byte[] bytes, URI redirectUri) {
		this.type = type;
		this.bytes = bytes;
		this.redirectUri = redirectUri;
	}

	public static TagCoverResult empty() {
		return new TagCoverResult(Type.EMPTY, null, null);
	}

	public static TagCoverResult bytes(byte[] bytes) {
		return new TagCoverResult(Type.BYTES, bytes, null);
	}

	public static TagCoverResult redirect(URI uri) {
		return new TagCoverResult(Type.REDIRECT, null, uri);
	}

	public boolean isEmpty() {
		return type == Type.EMPTY;
	}

	public boolean isRedirect() {
		return type == Type.REDIRECT;
	}

	public boolean isBytes() {
		return type == Type.BYTES;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public URI getRedirectUri() {
		return redirectUri;
	}
}
