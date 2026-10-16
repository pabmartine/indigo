package com.martinia.indigo.metadata.application;

import com.martinia.indigo.BaseIndigoTest;
import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.common.bus.command.domain.ports.CommandBus;
import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.common.util.DataUtils;
import com.martinia.indigo.metadata.application.commands.StartFillAuthorsMetadataUseCaseImpl;
import com.martinia.indigo.metadata.application.commands.StartFillBooksMetadataUseCaseImpl;
import com.martinia.indigo.metadata.domain.model.*;
import com.martinia.indigo.metadata.domain.model.commands.*;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.CancellationException;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MetadataExecutionIntegrationTest extends BaseIndigoTest {
    @MockBean private CommandBus commands;
    @MockBean private DataUtils dataUtils;
    @Resource(name = "mongoTemplate") private MongoTemplate mongo;
    @Resource private MetadataExecutionService executions;
    @Resource private StartFillBooksMetadataUseCaseImpl books;
    @Resource private StartFillAuthorsMetadataUseCaseImpl authors;
    private Object originalBooksState;
    private Object originalAuthorsState;

    @BeforeEach void setup() {
        mongo.dropCollection("metadataExecutions");
        originalBooksState = ReflectionTestUtils.getField(books, "metadataSingleton");
        originalAuthorsState = ReflectionTestUtils.getField(authors, "metadataSingleton");
    }

    @AfterEach void restore() {
        ReflectionTestUtils.setField(books, "metadataSingleton", originalBooksState);
        ReflectionTestUtils.setField(authors, "metadataSingleton", originalAuthorsState);
        mongo.dropCollection("metadataExecutions");
    }

    @Test void booksResumeAfterLossOfMemoryAndResetOnlyAfterTheWholeCycle() {
        var one = bookRepository.save(BookMongoEntity.builder().title("One").build());
        var two = bookRepository.save(BookMongoEntity.builder().title("Two").build());
        when(commands.executeAndWait(any(FindBookMetadataCommand.class)))
                .thenReturn(MetadataItemResult.NOT_FOUND).thenThrow(new CancellationException());
        runBooks(BookMetadataScope.ALL);
        assertThat(mongo.findAll(Document.class, "metadataExecutions")).singleElement()
                .satisfies(item -> assertThat(item.getDate("lastExecution")).isNotNull());
        String inspected = mongo.findAll(Document.class, "metadataExecutions").getFirst().getString("entityId");
        String remaining = inspected.equals(one.getId()) ? two.getId() : one.getId();

        reset(commands);
        when(commands.executeAndWait(any(FindBookMetadataCommand.class))).thenReturn(MetadataItemResult.ERROR);
        runBooks(BookMetadataScope.ALL); // Fresh singleton simulates a backend restart.
        verify(commands).executeAndWait(org.mockito.ArgumentMatchers.<FindBookMetadataCommand>argThat(
                command -> remaining.equals(command.getBookId())));
        assertThat(mongo.findAll(Document.class, "metadataExecutions")).hasSize(1);

        reset(commands);
        when(commands.executeAndWait(any(FindBookMetadataCommand.class))).thenReturn(MetadataItemResult.SKIPPED);
        runBooks(BookMetadataScope.ALL);
        verify(commands).executeAndWait(org.mockito.ArgumentMatchers.<FindBookMetadataCommand>argThat(
                command -> remaining.equals(command.getBookId())));
        assertThat(mongo.findAll(Document.class, "metadataExecutions")).hasSize(2);

        reset(commands);
        when(commands.executeAndWait(any(FindBookMetadataCommand.class))).thenReturn(MetadataItemResult.SKIPPED);
        runBooks(BookMetadataScope.ALL);
        verify(commands, times(2)).executeAndWait(any(FindBookMetadataCommand.class));
    }

    @Test void authorsResumeAndModesHaveIndependentCheckpoints() {
        authorRepository.save(AuthorMongoEntity.builder().id("one").name("One").build());
        authorRepository.save(AuthorMongoEntity.builder().id("two").name("Two").build());
        when(commands.executeAndWait(any(FindAuthorMetadataCommand.class)))
                .thenReturn(MetadataItemResult.NOT_FOUND).thenThrow(new CancellationException());
        runAuthors(true);
        assertThat(mongo.findAll(Document.class, "metadataExecutions")).hasSize(1);

        reset(commands);
        when(commands.executeAndWait(any(FindAuthorMetadataCommand.class))).thenReturn(MetadataItemResult.FOUND);
        runAuthors(true);
        verify(commands).executeAndWait(any(FindAuthorMetadataCommand.class));

        reset(commands);
        when(commands.executeAndWait(any(FindAuthorMetadataCommand.class))).thenReturn(MetadataItemResult.SKIPPED);
        runAuthors(false);
        verify(commands, times(2)).executeAndWait(any(FindAuthorMetadataCommand.class));
    }

    @Test void newEntitiesAndChangedSelectionsDoNotResetAnUnfinishedCycle() {
        executions.inspected("BOOKS:ALL", "one");
        assertThat(executions.pending("BOOKS:ALL", List.of("one", "two", "new")))
                .containsExactly("two", "new");
        assertThat(executions.pending("BOOKS:INCOMPLETE", List.of("one", "two")))
                .containsExactly("one", "two");
        assertThat(executions.pending("BOOKS:ALL", List.of())).isEmpty();
        assertThat(mongo.findAll(Document.class, "metadataExecutions")).hasSize(1);
    }

    @Test void replacedRunCannotCheckpointItsOutstandingRequest() {
        bookRepository.save(BookMongoEntity.builder().title("One").build());
        MetadataSingleton state = new MetadataSingleton();
        ReflectionTestUtils.setField(books, "metadataSingleton", state);
        long run = state.start("FULL", "BOOKS");
        when(commands.executeAndWait(any(FindBookMetadataCommand.class))).thenAnswer(call -> {
            state.start("PARTIAL", "BOOKS");
            return MetadataItemResult.FOUND;
        });
        books.start(BookMetadataScope.ALL, MetadataMergePolicy.FILL_MISSING, DynamicMetadataPolicy.REFRESH_IF_STALE, run);
        assertThat(mongo.findAll(Document.class, "metadataExecutions")).isEmpty();
    }

    @Test void authorErrorsStayPendingAndDoNotBlockOtherAuthorsDuringWikipediaPause() {
        authorRepository.save(AuthorMongoEntity.builder().id("one").name("One").build());
        authorRepository.save(AuthorMongoEntity.builder().id("two").name("Two").build());
        when(commands.executeAndWait(any(FindAuthorMetadataCommand.class)))
                .thenReturn(MetadataItemResult.ERROR, MetadataItemResult.FOUND);
        runAuthors(true);
        assertThat(mongo.findAll(Document.class, "metadataExecutions")).hasSize(1);
        reset(commands);
        when(commands.executeAndWait(any(FindAuthorMetadataCommand.class))).thenReturn(MetadataItemResult.NOT_FOUND);
        runAuthors(true);
        verify(commands).executeAndWait(any(FindAuthorMetadataCommand.class));
        assertThat(mongo.findAll(Document.class, "metadataExecutions")).hasSize(2);
    }

    @Test void deferredBooksRemainPendingAcrossRunsAndHaveTheirOwnCounter() {
        bookRepository.save(BookMongoEntity.builder().title("One").build());
        bookRepository.save(BookMongoEntity.builder().title("Two").build());
        when(commands.executeAndWait(any(FindBookMetadataCommand.class)))
                .thenReturn(MetadataItemResult.DEFERRED, MetadataItemResult.FOUND);
        runBooks(BookMetadataScope.ALL);
        var state = (MetadataSingleton) ReflectionTestUtils.getField(books, "metadataSingleton");
        assertThat(state.getDeferred()).isEqualTo(1);
        assertThat(state.getErrors()).isZero();
        assertThat(state.getRuns().get("FULL:BOOKS")).containsEntry("deferred", 1L);
        assertThat(state.isRunning()).isFalse();
        assertThat(mongo.findAll(Document.class, "metadataExecutions")).hasSize(1);
        reset(commands);
        when(commands.executeAndWait(any(FindBookMetadataCommand.class))).thenReturn(MetadataItemResult.FOUND);
        runBooks(BookMetadataScope.ALL);
        verify(commands).executeAndWait(any(FindBookMetadataCommand.class));
        assertThat(mongo.findAll(Document.class, "metadataExecutions")).hasSize(2);
        state = (MetadataSingleton) ReflectionTestUtils.getField(books, "metadataSingleton");
        assertThat(state.getDeferred()).isZero();
    }

    @Test void ratingSelectionIgnoresMissingSynopsisAndIncludesOtherwiseCompleteUnratedBooks() {
        var rated = bookRepository.save(BookMongoEntity.builder().title("Rated without synopsis").ratingAverage(4F).build());
        var unrated = bookRepository.save(BookMongoEntity.builder().title("Unrated").authors(List.of("Author"))
                .comment("Synopsis").pubDate(new java.util.Date()).pages(100).tags(List.of("Fiction"))
                .image("cover").languages(List.of("es")).isbn13(List.of("9780306406157")).build());
        assertThat(bookRepository.findBooksWithIncompleteMetadata()).extracting(BookMongoEntity::getId)
                .contains(unrated.getId()).doesNotContain(rated.getId());
    }

    @Test void repeatedQuotaDeferralsStopTheRunAndLeaveAllPendingForNextExecution() {
        for (int i = 0; i < 25; i++) bookRepository.save(BookMongoEntity.builder().title("Book " + i).build());
        when(commands.executeAndWait(any(FindBookMetadataCommand.class))).thenReturn(MetadataItemResult.DEFERRED);
        runBooks(BookMetadataScope.ALL);
        verify(commands, times(10)).executeAndWait(any(FindBookMetadataCommand.class));
        assertThat(mongo.findAll(Document.class, "metadataExecutions")).isEmpty();
        var state = (MetadataSingleton) ReflectionTestUtils.getField(books, "metadataSingleton");
        assertThat(state.getCurrent()).isEqualTo(10);
        assertThat(state.getTotal()).isEqualTo(25);
        assertThat(state.getMessage()).startsWith("Recorrido detenido:");
        assertThat(state.isRunning()).isFalse();
        reset(commands);
        when(commands.executeAndWait(any(FindBookMetadataCommand.class))).thenReturn(MetadataItemResult.FOUND);
        runBooks(BookMetadataScope.ALL);
        verify(commands, times(25)).executeAndWait(any(FindBookMetadataCommand.class));
    }

    private void runBooks(BookMetadataScope scope) {
        MetadataSingleton state = new MetadataSingleton();
        ReflectionTestUtils.setField(books, "metadataSingleton", state);
        long run = state.start(scope == BookMetadataScope.ALL ? "FULL" : "PARTIAL", "BOOKS");
        books.start(scope, MetadataMergePolicy.FILL_MISSING, DynamicMetadataPolicy.REFRESH_IF_STALE, run);
    }

    private void runAuthors(boolean override) {
        MetadataSingleton state = new MetadataSingleton();
        ReflectionTestUtils.setField(authors, "metadataSingleton", state);
        authors.start(override, "es", state.start(override ? "FULL" : "PARTIAL", "AUTHORS"));
    }
}
