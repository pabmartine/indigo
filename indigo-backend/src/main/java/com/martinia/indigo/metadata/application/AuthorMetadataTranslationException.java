package com.martinia.indigo.metadata.application;

/** A failed biography translation must not discard an independently usable photo. */
public class AuthorMetadataTranslationException extends IllegalStateException {
    private final String[] partialMetadata;

    public AuthorMetadataTranslationException(String message, String image, String provider, Throwable cause) {
        super(message, cause);
        partialMetadata = new String[] { null, image, provider };
    }

    public String[] getPartialMetadata() {
        return partialMetadata.clone();
    }
}
