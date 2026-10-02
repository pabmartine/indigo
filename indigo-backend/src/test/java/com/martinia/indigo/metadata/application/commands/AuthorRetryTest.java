package com.martinia.indigo.metadata.application.commands;

import com.martinia.indigo.author.domain.ports.repositories.AuthorRepository;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.book.domain.ports.repositories.BookRepository;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import com.martinia.indigo.metadata.domain.model.commands.FindAuthorMetadataCommand;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.Pageable;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class AuthorRetryTest {
    @Test
    void recordsTranslationFailureWithoutWikipediaWaitOrRetry() {
        var useCase = new StartFillAuthorsMetadataUseCaseImpl();
        var state = new MetadataSingleton();
        long run = state.start("metadata", "authors");
        var authors = mock(AuthorRepository.class);
        var books = mock(BookRepository.class);
        var bus = mock(CommandBus.class);
        ReflectionTestUtils.setField(useCase, "metadataSingleton", state);
        ReflectionTestUtils.setField(useCase, "authorRepository", authors);
        ReflectionTestUtils.setField(useCase, "bookRepository", books);
        ReflectionTestUtils.setField(useCase, "commandBus", bus);
        when(books.getBookLanguages()).thenReturn(List.of("es"));
        when(authors.count(anyList())).thenReturn(1L);
        when(authors.findAll(anyList(), any(Pageable.class))).thenReturn(List.of(AuthorMongoEntity.builder().id("pending").name("Author").build()));
        when(bus.executeAndWait(any(FindAuthorMetadataCommand.class))).thenReturn(MetadataItemResult.ERROR);
        useCase.start(false, "es", run);
        verify(bus, times(1)).executeAndWait(any(FindAuthorMetadataCommand.class));
        assertThat(state.getCurrent()).isEqualTo(1);
        assertThat(state.getFound()).isZero();
        assertThat(state.getErrors()).isEqualTo(1);
    }
}
