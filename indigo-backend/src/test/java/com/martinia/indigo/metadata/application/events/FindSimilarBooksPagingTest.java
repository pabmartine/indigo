package com.martinia.indigo.metadata.application.events;

import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
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
    when(repository.findSimilarCandidates(any(), eq(0)))
        .thenReturn(Collections.nCopies(100, mismatch));
    when(repository.findSimilarCandidates(any(), eq(1)))
        .thenReturn(List.of(match));
    useCase.findSimilarBooks("target", "Title@;@Author");
    verify(repository).updateSimilar("target", List.of("match"));
    verify(repository, never()).save(any());
    verify(repository, times(2)).findSimilarCandidates(any(), anyInt());
  }
}
