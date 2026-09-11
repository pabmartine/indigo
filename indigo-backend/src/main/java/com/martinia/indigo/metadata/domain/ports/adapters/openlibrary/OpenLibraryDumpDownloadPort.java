package com.martinia.indigo.metadata.domain.ports.adapters.openlibrary;

import java.nio.file.Path;
import java.util.function.BooleanSupplier;
import java.util.function.LongConsumer;

import com.martinia.indigo.metadata.domain.model.OpenLibraryDownloadResult;
import com.martinia.indigo.metadata.domain.model.OpenLibraryRemoteFile;

public interface OpenLibraryDumpDownloadPort {
	OpenLibraryRemoteFile inspect(String url);

	OpenLibraryDownloadResult download(String url, Path target, BooleanSupplier cancelled, LongConsumer progress);
}
