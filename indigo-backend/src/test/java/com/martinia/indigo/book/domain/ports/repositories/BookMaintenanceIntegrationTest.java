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
  @jakarta.annotation.Resource
  private org.springframework.test.web.servlet.MockMvc mockMvc;

  @Test
  @org.springframework.security.test.context.support.WithMockUser
  void summariesExcludeHeavyFieldsAndCoverReadsOnlyImage() throws Exception {
    var book = new BookMongoEntity();
    book.setTitle("Summary book");
    book.setImage(java.util.Base64.getEncoder().encodeToString("cover-bytes".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    book.setComment("A large description");
    book.setAuthors(List.of("Author"));
    book.setTags(List.of("Fiction"));
    book.setLanguages(List.of("spa"));
    book.setPages(321);
    book = bookRepository.save(book);
    var summary = bookRepository.findSummary(null, 0, 10, "id", "desc").getFirst();
    assertEquals(book.getId(), summary.getId());
    assertEquals(List.of("Author"), summary.getAuthors());
    assertEquals(321, summary.getPages());
    assertNull(summary.getImage());
    assertNull(summary.getComment());
    var page = bookRepository.findSummaryPage(null, 0, 10, "id", "desc");
    assertEquals(1, page.total());
    assertNull(page.items().getFirst().getImage());
    var cover = bookRepository.findCoverById(book.getId()).orElseThrow();
    assertEquals(book.getImage(), cover.getImage());
    assertNull(cover.getTitle());
    assertNull(cover.getAuthors());
    assertNull(cover.getComment());
    for (String suffix : List.of("/summary", "/summary/page")) {
      String itemPath = suffix.endsWith("page") ? "$.items[0]" : "$[0]";
      mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
          .post("/api/book/all/advance" + suffix)
          .contentType(org.springframework.http.MediaType.APPLICATION_JSON).content("{}")
          .param("page", "0").param("size", "10").param("sort", "id").param("order", "desc"))
          .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
          .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath(itemPath + ".title").value("Summary book"))
          .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath(itemPath + ".image").doesNotExist());
    }
  }

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
