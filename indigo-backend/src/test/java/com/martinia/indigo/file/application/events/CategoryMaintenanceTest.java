package com.martinia.indigo.file.application.events;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.infrastructure.mongo.entities.NumBooksMongo;
import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.tag.domain.ports.repositories.TagRepository;
import com.martinia.indigo.tag.infrastructure.mongo.entities.TagMongoEntity;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CategoryMaintenanceTest {
  @Test
  void optionalReconciliationContinuesAcrossBatchesAndUsesPartialUpdates(
      @org.junit.jupiter.api.io.TempDir java.nio.file.Path root) throws Exception {
    try (var zip = new java.util.zip.ZipOutputStream(java.nio.file.Files.newOutputStream(root.resolve("book.epub")))) {
      zip.putNextEntry(new java.util.zip.ZipEntry("content.opf"));
      zip.write(("<package xmlns:dc=\"http://purl.org/dc/elements/1.1/\"><metadata>"
          + "<dc:title>Example</dc:title><dc:subject>New category</dc:subject>"
          + "</metadata></package>").getBytes(java.nio.charset.StandardCharsets.UTF_8));
      zip.closeEntry();
    }
    var books = mock(BookRepository.class);
    var useCase = new SaveTagEpubFileEventUseCaseImpl();
    ReflectionTestUtils.setField(useCase, "bookRepository", books);
    ReflectionTestUtils.setField(useCase, "tagRepository", mock(TagRepository.class));
    ReflectionTestUtils.setField(useCase, "libraryPath", root.toString());
    ReflectionTestUtils.setField(useCase, "reconcileCategoriesOnStartup", true);
    var missing = new BookMongoEntity();
    missing.setId("a");
    missing.setPath(root.resolve("missing").toString());
    var book = new BookMongoEntity();
    book.setId("b");
    book.setPath(root.toString());
    book.setTags(List.of("Old category"));
    when(books.findReconciliationBatch(null)).thenReturn(List.of(missing));
    when(books.findReconciliationBatch("a")).thenReturn(List.of(book));
    when(books.findReconciliationBatch("b")).thenReturn(List.of());
    useCase.rebuildOnStartup();
    verify(books).updateReconciledMetadata(book, true, false);
    assertEquals(List.of("New category"), book.getTags());
    verify(books, never()).save(any());
    verify(books, never()).findAll();
  }

  @Test
  void combinesBatchesNormalizesCategoriesAndSkipsUnchangedWrites() {
    var books = mock(BookRepository.class);
    var tags = mock(TagRepository.class);
    var useCase = new SaveTagEpubFileEventUseCaseImpl();
    ReflectionTestUtils.setField(useCase, "bookRepository", books);
    ReflectionTestUtils.setField(useCase, "tagRepository", tags);
    ReflectionTestUtils.setField(useCase, "uploadEpubFilesSingleton", mock(UploadEpubFilesSingleton.class));
    var first = new BookMongoEntity();
    first.setId("a");
    first.setTags(List.of(" Fiction ", "FICTION"));
    first.setLanguages(List.of("spa"));
    var second = new BookMongoEntity();
    second.setId("b");
    second.setTags(List.of("fiction", "History"));
    second.setLanguages(List.of("eng"));
    when(books.findCategoryBatch(null)).thenReturn(List.of(first));
    when(books.findCategoryBatch("a")).thenReturn(List.of(second));
    when(books.findCategoryBatch("b")).thenReturn(List.of());
    var existing = TagMongoEntity.builder().name("History")
        .numBooks(NumBooksMongo.builder().total(1).languages(Map.of("eng", 1)).build()).build();
    when(tags.findAll()).thenReturn(List.of(existing));
    useCase.rebuildOnStartup();
    var saved = ArgumentCaptor.forClass(TagMongoEntity.class);
    verify(tags).save(saved.capture());
    assertEquals("Fiction", saved.getValue().getName());
    assertEquals(2, saved.getValue().getNumBooks().getTotal());
    assertEquals(Map.of("spa", 1, "eng", 1), saved.getValue().getNumBooks().getLanguages());
    verify(books, never()).findAll();
    verify(books, never()).findReconciliationBatch(any());
    verify(books).findCategoryBatch("b");
  }
}
