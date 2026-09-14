package com.martinia.indigo.book.domain.ports.repositories;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.domain.model.Search;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookMaintenanceIntegrationTest extends BaseIndigoTest {
  @Test
  void maintenanceTraversesObjectIdsWithoutLoadingCoversAndUpdatesOnlyChangedFields() {
    var books = new ArrayList<BookMongoEntity>();
    for (int i = 0; i < 205; i++) {
      var book = new BookMongoEntity();
      book.setTitle(String.format("Book %03d", i));
      book.setImage("cover-data");
      book.setComment("keep comment");
      book.setTags(List.of("Fiction"));
      book.setLanguages(List.of("spa"));
      book.setMetadataMatchStatus("MATCHED");
      books.add(book);
    }
    bookRepository.saveAll(books);
    var ids = new HashSet<String>();
    String cursor = null;
    while (true) {
      var batch = bookRepository.findCategoryBatch(cursor);
      if (batch.isEmpty()) break;
      assertTrue(batch.size() <= 100);
      for (var book : batch) {
        assertTrue(ids.add(book.getId()));
        assertNull(book.getImage());
        assertNull(book.getComment());
        assertEquals(List.of("Fiction"), book.getTags());
      }
      cursor = batch.getLast().getId();
    }
    assertEquals(205, ids.size());
    var projected = bookRepository.findReconciliationBatch(null).getFirst();
    assertNull(projected.getImage());
    projected.setTags(List.of("Updated"));
    bookRepository.updateReconciledMetadata(projected, true, false);
    bookRepository.updateSimilar(projected.getId(), List.of("similar"));
    bookRepository.updateRecommendations(projected.getId(), List.of("recommendation"));
    var saved = bookRepository.findById(projected.getId()).orElseThrow();
    assertEquals("cover-data", saved.getImage());
    assertEquals("keep comment", saved.getComment());
    assertEquals("MATCHED", saved.getMetadataMatchStatus());
    assertEquals(List.of("Updated"), saved.getTags());
    assertEquals(List.of("similar"), saved.getSimilar());
    assertEquals(List.of("recommendation"), saved.getRecommendations());
    projected.setIdentifiers(Map.of("isbn", List.of("123")));
    projected.setIsbn13(List.of("123"));
    bookRepository.updateReconciledMetadata(projected, false, true);
    saved = bookRepository.findById(projected.getId()).orElseThrow();
    assertNull(saved.getMetadataMatchStatus());
    assertEquals(List.of("123"), saved.getIsbn13());
    assertEquals("cover-data", saved.getImage());
    assertNull(bookRepository.findRecommendationSource(saved.getId()).orElseThrow().getImage());
  }

  @Test
  void listingReturnsSortedPageWithCoverAndUnpaginatedFilteredTotal() {
    for (int i = 0; i < 5; i++) {
      var book = new BookMongoEntity();
      book.setTitle("Book " + i);
      book.setImage("cover");
      book.setLanguages(List.of(i == 4 ? "eng" : "spa"));
      bookRepository.save(book);
    }
    var search = new Search();
    search.setLanguages(List.of("spa"));
    var page = bookRepository.findAllPage(search, 1, 2, "title", "asc");
    assertEquals(4, page.total());
    assertEquals(List.of("Book 2", "Book 3"), page.items().stream().map(b -> b.getTitle()).toList());
    assertEquals("cover", page.items().getFirst().getImage());
    assertEquals(4, bookRepository.findAllPage(search, 5, 2, "title", "asc").total());
  }
}
