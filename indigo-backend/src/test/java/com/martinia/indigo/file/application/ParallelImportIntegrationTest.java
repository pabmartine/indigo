package com.martinia.indigo.file.application;

import com.martinia.indigo.BaseIndigoIntegrationTest;
import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.common.util.ImageUtils;
import com.martinia.indigo.file.domain.ports.usecases.events.SaveTagEpubFileEventUseCase;
import jakarta.annotation.Resource;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ParallelImportIntegrationTest extends BaseIndigoIntegrationTest {
    private static final Path ROOT = root();
    @Resource private ParallelEpubImporter importer;
    @Resource private PreparedEpubReader reader;
    @Resource private UploadEpubFilesSingleton progress;
    @Resource private PendingImportService pending;
    @MockBean private ImageUtils images;
    @SpyBean private SaveTagEpubFileEventUseCase categories;
    @DynamicPropertySource static void paths(DynamicPropertyRegistry registry) {
        registry.add("book.library.path", () -> ROOT.resolve("library").toString());
        registry.add("book.library.uploads", () -> ROOT.resolve("uploads").toString());
        registry.add("book.library.import-workers", () -> "4");
    }
    private static Path root() { try { return Files.createTempDirectory("indigo-parallel-test-"); } catch (Exception e) { throw new IllegalStateException(e); } }
    @BeforeEach void prepare() throws Exception {
        Files.createDirectories(ROOT.resolve("uploads")); Files.createDirectories(ROOT.resolve("library"));
        clearInvocations(categories);
    }
    private Path epub(String filename, String title, int version) throws Exception {
        Path file = ROOT.resolve("uploads").resolve(filename);
        String opf = """
            <package xmlns:dc="http://purl.org/dc/elements/1.1/">
            <metadata><dc:title>%s</dc:title><dc:creator>Same Author</dc:creator><dc:language>es</dc:language>
            <dc:subject>Fantasía</dc:subject>
            <meta name="calibre:user_metadata:#version" content="{&quot;#value#&quot;: %d, &quot;unused&quot;: 0}"/>
            </metadata><manifest/></package>
            """.formatted(title, version);
        try (var zip = new ZipOutputStream(Files.newOutputStream(file))) {
            var entry = new ZipEntry("content.opf"); entry.setTime(0); zip.putNextEntry(entry);
            zip.write(opf.getBytes(StandardCharsets.UTF_8)); zip.closeEntry();
            zip.putNextEntry(new ZipEntry("cover.jpg")); zip.write(new byte[]{1,2,3}); zip.closeEntry();
        }
        return file;
    }
    @Test void concurrentVersionsRemainOneBookAndCategoriesRebuildOnlyOnce() throws Exception {
        var files = List.of(epub("v1.epub", "Same Book", 1), epub("v3.epub", "Same Book", 3),
                epub("v2.epub", "Same Book", 2), epub("other.epub", "Other Book", 1));
        progress.start(files.size()); progress.beginManagedProcessing();
        try { importer.process(files); } finally { progress.endManagedProcessing(); }
        assertEquals(2, bookRepository.count());
        var book = bookRepository.findByTitleIgnoreCase("Same Book").get(0);
        assertEquals(3, book.getVersion());
        assertEquals(2, authorRepository.findByName("Same Author").orElseThrow().getNumBooks().getTotal());
        var tag = tagRepository.findByName("Fantasía").get(0);
        assertEquals(2, tag.getNumBooks().getTotal());
        assertEquals(2, tag.getNumBooks().getLanguages().get("es"));
        verify(categories, times(1)).rebuildAfterBatch(anyList());
        verify(images, times(2)).saveCoverAndGetThumbnail(any(byte[].class), any(Path.class));
        assertTrue(pending.pending().isEmpty());
        pending.retry(book.getId());
        assertEquals(2, tagRepository.findByName("Fantasía").get(0).getNumBooks().getTotal());
        assertEquals(4, progress.getProcessedItems());
    }
    @Test void preparedFilesHaveIsolatedDirectoriesAndImagesAreLazy() throws Exception {
        try (var first = reader.read(epub("one.epub", "One", 1)); var second = reader.read(epub("two.epub", "Two", 1))) {
            assertNotEquals(first.path().getParent(), second.path().getParent());
            verifyNoInteractions(images);
            first.loadImages();
            verify(images).saveCoverAndGetThumbnail(any(byte[].class), eq(first.path().getParent().resolve("cover.jpg")));
        }
    }
    @Test void pollingDoesNotFinishManagedImportBeforeRelatedTasks() {
        progress.start(1); progress.beginManagedProcessing();
        progress.addMove();
        assertEquals(100, progress.getCurentStatus());
        assertTrue(progress.isRunning());
        progress.endManagedProcessing(); assertFalse(progress.isRunning());
    }
}
