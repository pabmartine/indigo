package com.martinia.indigo.metadata.infrastructure.adapters.openlibrary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.LongConsumer;

import com.martinia.indigo.metadata.domain.model.OpenLibraryDownloadResult;
import com.martinia.indigo.metadata.domain.model.OpenLibraryIndexCancelledException;
import com.martinia.indigo.metadata.domain.model.OpenLibraryRemoteFile;
import com.martinia.indigo.metadata.domain.ports.adapters.openlibrary.OpenLibraryDumpDownloadPort;
import org.springframework.stereotype.Component;

@Component
public class HttpOpenLibraryDumpDownloadAdapter implements OpenLibraryDumpDownloadPort {
	private static final int BUFFER_SIZE = 128 * 1024;
	private static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(10);
	private final HttpClient httpClient = HttpClient.newBuilder()
			.followRedirects(HttpClient.Redirect.NORMAL)
			.connectTimeout(Duration.ofSeconds(30))
			.build();

	@Override
	public OpenLibraryRemoteFile inspect(final String url) {
		try {
			final HttpRequest request = HttpRequest.newBuilder(URI.create(url))
					.timeout(REQUEST_TIMEOUT)
					.method("HEAD", HttpRequest.BodyPublishers.noBody())
					.build();
			final HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
			if (response.statusCode() == 405 || response.statusCode() == 501) {
				return inspectWithRange(url);
			}
			if (response.statusCode() < 200 || response.statusCode() >= 400) {
				throw new IllegalStateException("Unexpected HTTP status " + response.statusCode() + " for " + url);
			}
			return new OpenLibraryRemoteFile(contentLength(response), header(response, "Last-Modified"),
					header(response, "ETag"));
		}
		catch (IOException exception) {
			throw new IllegalStateException("Could not inspect " + url, exception);
		}
		catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while inspecting " + url, exception);
		}
	}

	@Override
	public OpenLibraryDownloadResult download(final String url, final Path target,
			final BooleanSupplier cancelled, final LongConsumer progress) {
		final Path partial = target.resolveSibling(target.getFileName() + ".part");
		try {
			Files.createDirectories(target.getParent());
			if (cancelled.getAsBoolean()) {
				throw new OpenLibraryIndexCancelledException();
			}
			if (Files.isRegularFile(target)) {
				return new OpenLibraryDownloadResult(target, Files.size(target), null, null);
			}
			long existingBytes = Files.isRegularFile(partial) ? Files.size(partial) : 0L;
			HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(url)).timeout(REQUEST_TIMEOUT).GET();
			if (existingBytes > 0) {
				requestBuilder.header("Range", "bytes=" + existingBytes + '-');
			}
			final HttpResponse<InputStream> response = httpClient.send(requestBuilder.build(),
					HttpResponse.BodyHandlers.ofInputStream());
			if (response.statusCode() == 416 && existingBytes > 0) {
				try (InputStream ignored = response.body()) {
					final long remoteSize = unsatisfiedRangeSize(response);
					if (remoteSize == existingBytes) {
						Files.move(partial, target, StandardCopyOption.ATOMIC_MOVE,
								StandardCopyOption.REPLACE_EXISTING);
						return new OpenLibraryDownloadResult(target, existingBytes,
								header(response, "Last-Modified"), header(response, "ETag"));
					}
				}
				throw new IllegalStateException("Server rejected resume for incomplete file " + partial);
			}
			if (response.statusCode() != 200 && response.statusCode() != 206) {
				try (InputStream ignored = response.body()) {
					throw new IllegalStateException("Unexpected HTTP status " + response.statusCode() + " for " + url);
				}
			}
			final boolean append = response.statusCode() == 206 && existingBytes > 0;
			if (append && !hasExpectedRangeStart(response, existingBytes)) {
				try (InputStream ignored = response.body()) {
					throw new IllegalStateException("Server returned an invalid byte range while resuming " + url);
				}
			}
			if (!append) {
				existingBytes = 0L;
			}
			final StandardOpenOption[] options = append
					? new StandardOpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.APPEND }
					: new StandardOpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };
			long downloaded = existingBytes;
			long responseBytes = 0L;
			final long expectedResponseBytes = contentLength(response);
			final long expectedTotalBytes = response.statusCode() == 206 ? contentRangeSize(response) : -1L;
			progress.accept(downloaded);
			try (InputStream input = response.body(); OutputStream output = Files.newOutputStream(partial, options)) {
				final byte[] buffer = new byte[BUFFER_SIZE];
				int read;
				while ((read = input.read(buffer)) != -1) {
					if (cancelled.getAsBoolean()) {
						throw new OpenLibraryIndexCancelledException();
					}
					output.write(buffer, 0, read);
					downloaded += read;
					responseBytes += read;
					progress.accept(downloaded);
				}
			}
			if (expectedResponseBytes >= 0 && responseBytes != expectedResponseBytes) {
				throw new IllegalStateException("Incomplete download for " + url + ": expected "
						+ expectedResponseBytes + " bytes, received " + responseBytes);
			}
			if (expectedTotalBytes >= 0 && downloaded != expectedTotalBytes) {
				throw new IllegalStateException("Incomplete resumed download for " + url + ": expected "
						+ expectedTotalBytes + " total bytes, received " + downloaded);
			}
			if (cancelled.getAsBoolean()) {
				throw new OpenLibraryIndexCancelledException();
			}
			Files.move(partial, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			return new OpenLibraryDownloadResult(target, downloaded, header(response, "Last-Modified"),
					header(response, "ETag"));
		}
		catch (OpenLibraryIndexCancelledException exception) {
			throw exception;
		}
		catch (IOException exception) {
			throw new IllegalStateException("Could not download " + url, exception);
		}
		catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while downloading " + url, exception);
		}
	}

	private OpenLibraryRemoteFile inspectWithRange(final String url) throws IOException, InterruptedException {
		final HttpRequest request = HttpRequest.newBuilder(URI.create(url))
				.timeout(REQUEST_TIMEOUT)
				.header("Range", "bytes=0-0")
				.GET()
				.build();
		final HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
		try (InputStream ignored = response.body()) {
			if (response.statusCode() != 200 && response.statusCode() != 206) {
				throw new IllegalStateException("Unexpected HTTP status " + response.statusCode() + " for " + url);
			}
			return new OpenLibraryRemoteFile(totalSize(response), header(response, "Last-Modified"),
					header(response, "ETag"));
		}
	}

	private long totalSize(final HttpResponse<?> response) {
		final long contentRangeSize = contentRangeSize(response);
		return contentRangeSize >= 0 ? contentRangeSize : contentLength(response);
	}

	private long contentRangeSize(final HttpResponse<?> response) {
		final String contentRange = header(response, "Content-Range");
		if (contentRange != null) {
			final int separator = contentRange.lastIndexOf('/');
			if (separator >= 0) {
				try {
					return Long.parseLong(contentRange.substring(separator + 1));
				}
				catch (NumberFormatException ignored) {
					// Fall back to Content-Length.
				}
			}
		}
		return -1L;
	}

	private boolean hasExpectedRangeStart(final HttpResponse<?> response, final long expectedStart) {
		final String contentRange = header(response, "Content-Range");
		return contentRange != null && contentRange.startsWith("bytes " + expectedStart + '-');
	}

	private long unsatisfiedRangeSize(final HttpResponse<?> response) {
		return totalSize(response);
	}

	private long contentLength(final HttpResponse<?> response) {
		return response.headers().firstValueAsLong("Content-Length").orElse(-1L);
	}

	private String header(final HttpResponse<?> response, final String name) {
		return Optional.ofNullable(response.headers().firstValue(name).orElse(null)).orElse(null);
	}
}
