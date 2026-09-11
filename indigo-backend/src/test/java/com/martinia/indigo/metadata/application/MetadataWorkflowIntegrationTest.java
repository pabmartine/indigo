package com.martinia.indigo.metadata.application;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.file.domain.ports.usecases.commands.ExtractEpubFileUseCase;
import com.martinia.indigo.metadata.domain.model.*;
import com.martinia.indigo.metadata.domain.model.commands.*;
import com.martinia.indigo.metadata.domain.ports.repositories.*;
import com.martinia.indigo.metadata.infrastructure.mongo.entities.*;
import com.martinia.indigo.metadata.application.openlibrary.OpenLibraryIndexManager;
import com.martinia.indigo.common.infrastructure.api.model.ReviewDto;
import jakarta.annotation.Resource;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.annotation.DirtiesContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Real ZIP extraction, event chain and Mongo persistence; external providers are controlled. */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MetadataWorkflowIntegrationTest extends BaseIndigoIntegrationTest {
    private static final Path ROOT = temporaryRoot();
    @Resource private ExtractEpubFileUseCase extract;
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.martinia.indigo.common.util.ImageUtils images;
    @Resource private OpenLibraryIndexJobRepository jobs;
    @Resource private OpenLibraryEditionMappingRepository mappings;
    @Resource private OpenLibraryRatingRepository ratings;
    @Resource private com.martinia.indigo.file.application.PendingImportService pendingImports;

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(booleans = {false, true})
    @org.springframework.security.test.context.support.WithMockUser(authorities = "ADMIN")
    void resumeInitialImportAfterDatabaseCommitWithoutDuplicatingAuthors(boolean manual) throws Exception {
        Files.createDirectories(ROOT.resolve("uploads"));
        Path source = epub("recovery.epub", 1);
        Path target = ROOT.resolve("library/Recovered Book");
        var book = bookRepository.save(com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity.builder()
                .title("Test Book").path(target.toString()).authors(List.of("Test Author"))
                .languages(List.of("es")).tags(List.of()).version(1).build());
        pendingImports.register(com.martinia.indigo.file.domain.model.events.EpubFileAddedEvent.builder()
                .bookId(book.getId()).sourcePath(source).targetPath(target).newBook(true).build());
        if (manual) {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/metadata/pending-imports"))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
            assertTrue(pendingImports.pending().stream().anyMatch(item -> book.getId().equals(item.getString("bookId"))));
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/metadata/pending-imports/" + book.getId() + "/retry"))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.pendingTasks").isEmpty());
        } else pendingImports.resume();
        await(() -> pendingImports.done(book.getId(), "fileDone") && pendingImports.done(book.getId(), "authorsDone")
                && pendingImports.done(book.getId(), "tagsDone"));
        assertTrue(Files.exists(target.resolve("recovery.epub")));
        assertFalse(Files.exists(source));
        if (manual) pendingImports.retry(book.getId()); else pendingImports.resume();
        assertFalse(pendingImports.pending().stream().anyMatch(item -> book.getId().equals(item.getString("bookId"))));
        assertEquals(1, authorRepository.findByName("Test Author").orElseThrow().getNumBooks().getTotal());
    }
    @Test @org.springframework.security.test.context.support.WithMockUser(authorities = "USER")
    void pendingImportsRequireAdministrator() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/metadata/pending-imports"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/metadata/pending-imports/book/retry"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
    }
    @DynamicPropertySource static void paths(DynamicPropertyRegistry registry) {
        registry.add("book.library.path", () -> ROOT.resolve("library").toString());
        registry.add("book.library.uploads", () -> ROOT.resolve("uploads").toString());
    }
    private static Path temporaryRoot() {
        try { return Files.createTempDirectory("indigo-workflow-test-"); }
        catch (java.io.IOException exception) { throw new IllegalStateException(exception); }
    }
    @Test void importEnrichUpgradeAndDiscardDuplicate() throws Exception {
        Files.createDirectories(ROOT.resolve("uploads"));
        Path first = epub("first.epub", 1);
        extract.extract(first);
        await(() -> bookRepository.count() == 1 && authorRepository.count() == 1);
        var book = bookRepository.findAll().get(0);
        Path installed = Path.of(book.getPath()).resolve("first.epub");
        await(() -> Files.exists(installed));
        jobs.save(OpenLibraryIndexJobMongoEntity.builder().id(OpenLibraryIndexManager.JOB_ID).activeVersion("workflow").build());
        mappings.save(OpenLibraryEditionMappingMongoEntity.builder().id("workflow-map").indexVersion("workflow")
                .isbn("9780306406157").workId("OL1W").editionId("OL1M").build());
        ratings.save(OpenLibraryRatingMongoEntity.builder().id("workflow-rating").indexVersion("workflow")
                .workId("OL1W").average(4.5F).count(20).build());
        assertEquals(MetadataItemResult.FOUND, commandBus.executeAndWait(FindBookMetadataCommand.builder().bookId(book.getId())
                .mergePolicy(MetadataMergePolicy.FILL_MISSING).dynamicPolicy(DynamicMetadataPolicy.REFRESH_IF_STALE).build()));
        assertEquals(4.5F, bookRepository.findById(book.getId()).orElseThrow().getRatingAverage());

        when(dataUtils.getData(contains("search/authors"))).thenReturn("{\"docs\":[{\"key\":\"OL1A\",\"name\":\"Test Author\"}]}");
        when(dataUtils.getData(contains("authors/OL1A.json"))).thenReturn("{\"bio\":\"Writer\"}");
        doReturn("Escritor de prueba").when(translateLibreTranslatePort).translate("Writer", "es");
        doReturn("en").when(detectLibreTranslatePort).detect("Writer");
        var author = authorRepository.findAll().get(0);
        commandBus.executeAndWait(FindAuthorMetadataCommand.builder().authorId(author.getId()).override(true).lang("es").build());
        assertEquals("Escritor de prueba", authorRepository.findById(author.getId()).orElseThrow().getDescription());
        assertEquals("OPEN_LIBRARY", authorRepository.findById(author.getId()).orElseThrow().getMetadataSources().get("description"));

        doReturn(List.of(ReviewDto.builder().name("Reader").comment("Reseña").rating(1).provider("GOODREADS")
                .sourceUrl("https://www.goodreads.com/review/show/1").language("es").build()))
                .when(findGoodReadsReviewsPort).getReviews(eq("es"), anyString(), anyList());
        commandBus.executeAndWait(FindReviewMetadataCommand.builder().bookId(book.getId()).override(true).lang("es").build());
        book = bookRepository.findById(book.getId()).orElseThrow();
        assertEquals(4.5F, book.getRating());
        assertEquals("https://www.goodreads.com/review/show/1", book.getReviews().get(0).getSourceUrl());
        String id = book.getId();
        Path newer = epub("different-name.epub", 2);
        extract.extract(newer);
        await(() -> bookRepository.findById(id).orElseThrow().getVersion() == 2);
        assertEquals(1, bookRepository.count());
        assertEquals(1, authorRepository.findById(author.getId()).orElseThrow().getNumBooks().getTotal());
        assertEquals(1, bookRepository.findById(id).orElseThrow().getReviews().size());
        byte[] installedBytes = Files.readAllBytes(installed);
        Path duplicate = epub("duplicate.epub", 2);
        extract.extract(duplicate);
        await(() -> { try (var files = Files.walk(ROOT.resolve("uploads"))) {
            return files.noneMatch(path -> path.toString().endsWith(".epub"));
        } catch (Exception exception) { return false; } });
        assertArrayEquals(installedBytes, Files.readAllBytes(installed));
        assertEquals(1, bookRepository.count());
    }
    private Path epub(String name, int version) throws Exception {
        Path path = ROOT.resolve("uploads").resolve(name);
        String opf = """
                <package xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:opf="http://www.idpf.org/2007/opf">
                <metadata><dc:title>Test Book</dc:title><dc:creator>Test Author</dc:creator><dc:language>es</dc:language>
                <dc:identifier opf:scheme="ISBN">9780306406157</dc:identifier>
                <meta name="calibre:user_metadata:#version" content="{&quot;#value#&quot;: %d, &quot;unused&quot;: 0}"/>
                </metadata><manifest/></package>
                """.formatted(version);
        try (var zip = new ZipOutputStream(Files.newOutputStream(path))) {
            zip.putNextEntry(new ZipEntry("content.opf"));
            zip.write(opf.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return path;
    }
    private void await(java.util.function.BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(10);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) Thread.sleep(25);
        assertTrue(condition.getAsBoolean(), "Workflow did not complete");
    }
}
