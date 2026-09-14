package com.martinia.indigo.metadata.application.events;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FindSimilarBooksPagingTest {
  @Test
  void findsMatchesBeyondFirstPage() {
    var repository = mock(BookRepository.class);
    var useCase = new FindSimilarBooksMetadataUseCaseImpl();
    ReflectionTestUtils.setField(useCase, "bookRepository", repository);
    var mismatch = new BookMongoEntity();
    mismatch.setAuthors(List.of("Other"));
    var match = new BookMongoEntity();
    match.setId("match");
    match.setAuthors(List.of("Author"));
    var target = new BookMongoEntity();
    when(repository.findAll(any(), eq(0), eq(100), eq("_id"), eq("asc")))
        .thenReturn(Collections.nCopies(100, mismatch));
    when(repository.findAll(any(), eq(1), eq(100), eq("_id"), eq("asc")))
        .thenReturn(List.of(match));
    when(repository.findById("target")).thenReturn(Optional.of(target));
    useCase.findSimilarBooks("target", "Title@;@Author");
    assertEquals(List.of("match"), target.getSimilar());
    verify(repository).save(target);
    verify(repository, times(2)).findAll(any(), anyInt(), eq(100), eq("_id"), eq("asc"));
  }
}
