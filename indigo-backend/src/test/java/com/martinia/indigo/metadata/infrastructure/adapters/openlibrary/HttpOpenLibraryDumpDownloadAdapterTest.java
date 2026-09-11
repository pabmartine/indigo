package com.martinia.indigo.metadata.infrastructure.adapters.openlibrary;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.martinia.indigo.metadata.domain.model.OpenLibraryDownloadResult;
import com.martinia.indigo.metadata.domain.model.OpenLibraryIndexCancelledException;
import com.martinia.indigo.metadata.domain.model.OpenLibraryRemoteFile;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpOpenLibraryDumpDownloadAdapterTest {
	private static final byte[] CONTENT = "open-library-dump".getBytes(StandardCharsets.UTF_8);

	@TempDir
	private Path temporaryDirectory;

	private HttpServer server;
	private String url;
	private HttpOpenLibraryDumpDownloadAdapter adapter;

	@BeforeEach
	void setUp() throws IOException {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		url = "http://127.0.0.1:" + server.getAddress().getPort() + "/dump";
		adapter = new HttpOpenLibraryDumpDownloadAdapter();
	}

	@AfterEach
	void tearDown() {
		server.stop(0);
	}

	@Test
	void resumesAPartialDownloadWithAByteRangeAndPublishesItAtomically() throws IOException {
		AtomicReference<String> requestedRange = new AtomicReference<>();
		server.createContext("/dump", exchange -> {
			requestedRange.set(exchange.getRequestHeaders().getFirst("Range"));
			int offset = 5;
			exchange.getResponseHeaders().add("Content-Range", "bytes " + offset + '-' + (CONTENT.length - 1)
					+ '/' + CONTENT.length);
			exchange.sendResponseHeaders(206, CONTENT.length - offset);
			exchange.getResponseBody().write(CONTENT, offset, CONTENT.length - offset);
			exchange.close();
		});
		server.start();
		Path target = temporaryDirectory.resolve("dump.txt.gz");
		Files.write(target.resolveSibling("dump.txt.gz.part"), Arrays.copyOf(CONTENT, 5));

		OpenLibraryDownloadResult result = adapter.download(url, target, () -> false, ignored -> {
		});

		assertThat(requestedRange).hasValue("bytes=5-");
		assertThat(result.size()).isEqualTo(CONTENT.length);
		assertThat(Files.readAllBytes(target)).isEqualTo(CONTENT);
		assertThat(target.resolveSibling("dump.txt.gz.part")).doesNotExist();
	}

	@Test
	void restartsFromZeroWhenTheServerIgnoresTheRange() throws IOException {
		server.createContext("/dump", exchange -> respond(exchange, 200, CONTENT));
		server.start();
		Path target = temporaryDirectory.resolve("dump.txt.gz");
		Files.writeString(target.resolveSibling("dump.txt.gz.part"), "stale");

		adapter.download(url, target, () -> false, ignored -> {
		});

		assertThat(Files.readAllBytes(target)).isEqualTo(CONTENT);
	}

	@Test
	void rejectsAResumeResponseThatStartsAtTheWrongByte() throws IOException {
		server.createContext("/dump", exchange -> {
			exchange.getResponseHeaders().add("Content-Range", "bytes 4-" + (CONTENT.length - 1) + '/'
					+ CONTENT.length);
			respond(exchange, 206, CONTENT);
		});
		server.start();
		Path target = temporaryDirectory.resolve("dump.txt.gz");
		Path partial = target.resolveSibling("dump.txt.gz.part");
		Files.write(partial, Arrays.copyOf(CONTENT, 5));

		assertThatThrownBy(() -> adapter.download(url, target, () -> false, ignored -> {
		})).isInstanceOf(IllegalStateException.class).hasMessageContaining("invalid byte range");
		assertThat(target).doesNotExist();
		assertThat(Files.size(partial)).isEqualTo(5L);
	}

	@Test
	void keepsThePartialFileAndDoesNotPublishWhenCancelled() {
		server.createContext("/dump", exchange -> respond(exchange, 200, CONTENT));
		server.start();
		Path target = temporaryDirectory.resolve("dump.txt.gz");
		AtomicInteger cancellationChecks = new AtomicInteger();

		assertThatThrownBy(() -> adapter.download(url, target, () -> cancellationChecks.incrementAndGet() > 1,
				ignored -> {
		})).isInstanceOf(OpenLibraryIndexCancelledException.class);
		assertThat(target).doesNotExist();
		assertThat(target.resolveSibling("dump.txt.gz.part")).exists();
	}

	@Test
	void inspectsSizeThroughARangeRequestWhenHeadIsUnsupported() {
		server.createContext("/dump", exchange -> {
			if ("HEAD".equals(exchange.getRequestMethod())) {
				exchange.sendResponseHeaders(405, -1);
				exchange.close();
				return;
			}
			exchange.getResponseHeaders().add("Content-Range", "bytes 0-0/" + CONTENT.length);
			exchange.getResponseHeaders().add("ETag", "fixture-etag");
			respond(exchange, 206, new byte[] { CONTENT[0] });
		});
		server.start();

		OpenLibraryRemoteFile remote = adapter.inspect(url);

		assertThat(remote.size()).isEqualTo(CONTENT.length);
		assertThat(remote.etag()).isEqualTo("fixture-etag");
	}

	private void respond(final HttpExchange exchange, final int status, final byte[] body) throws IOException {
		exchange.sendResponseHeaders(status, body.length);
		exchange.getResponseBody().write(body);
		exchange.close();
	}
}
