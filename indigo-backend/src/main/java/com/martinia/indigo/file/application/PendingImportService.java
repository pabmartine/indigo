package com.martinia.indigo.file.application;

import com.martinia.indigo.file.domain.model.events.EpubFileAddedEvent;
import jakarta.annotation.Resource;
import java.nio.file.*;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Service;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import lombok.extern.slf4j.Slf4j;

/** Durable outbox for the initial import's independent event consumers. */
@Service @Slf4j
public class PendingImportService {
    @org.springframework.beans.factory.annotation.Autowired private MongoTemplate mongo;
    @Value("${book.library.path}") private String library;
    @Value("${book.library.uploads}") private String uploads;
    @Resource private com.martinia.indigo.common.singletons.UploadEpubFilesSingleton uploadState;
    @org.springframework.beans.factory.annotation.Autowired private org.springframework.beans.factory.ObjectProvider<com.martinia.indigo.file.domain.ports.usecases.events.MoveEpubFileEventUseCase> mover;
    @org.springframework.beans.factory.annotation.Autowired private org.springframework.beans.factory.ObjectProvider<com.martinia.indigo.file.domain.ports.usecases.events.SaveAuthorEpubFileEventUseCase> authors;
    @org.springframework.beans.factory.annotation.Autowired private org.springframework.beans.factory.ObjectProvider<com.martinia.indigo.file.domain.ports.usecases.events.SaveTagEpubFileEventUseCase> tags;

    public java.util.List<Document> pending() {
        return mongo.find(new Query().with(org.springframework.data.domain.Sort.by("createdAt").ascending()), Document.class, "pendingImports")
                .stream().map(this::status).filter(item -> !item.getList("pendingTasks", String.class).isEmpty()).toList();
    }

    private Document status(Document entry) {
        String id = entry.getString("bookId");
        var tasks = java.util.stream.Stream.of("fileDone", "authorsDone", "tagsDone").filter(task -> !done(id, task)).toList();
        return new Document("bookId", id).append("fileName", Path.of(entry.getString("source")).getFileName().toString())
                .append("createdAt", entry.get("createdAt")).append("pendingTasks", tasks)
                .append("lastError", entry.getString("lastError"));
    }

    /** Synchronous retries keep the lock until each consumer's transaction has committed. */
    public synchronized Document retry(String id) {
        if (uploadState.isRunning() || uploadState.isManagedProcessing()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Espera a que termine la importación en curso");
        return finishManaged(id);
    }

    public synchronized Document finishManaged(String id) {
        return finishManaged(id, true);
    }

    public synchronized void completeBatchCategories(java.util.List<String> ids) {
        if (!ids.isEmpty()) tags.getObject().rebuildAfterBatch(ids);
    }

    public synchronized Document finishManaged(String id, boolean updateCategories) {
        Document entry = mongo.findById("book:" + id, Document.class, "pendingImports");
        if (entry == null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Importación no encontrada");
        try {
            Path source = Path.of(entry.getString("source")).toAbsolutePath().normalize();
            Path target = Path.of(entry.getString("target")).toAbsolutePath().normalize();
            validatePath(source, Path.of(uploads));
            validatePath(target, Path.of(library));
            if (!done(id, "fileDone")) mover.getObject().move(source, target);
            if (!done(id, "authorsDone")) authors.getObject().save(id, entry.getString("authorImage"), true);
            if (updateCategories && !done(id, "tagsDone")) tags.getObject().save(id, true);
            String error = status(entry).getList("pendingTasks", String.class).stream().noneMatch(task -> updateCategories || !"tagsDone".equals(task))
                    ? null : "Quedan tareas pendientes; consulta el log para conocer el detalle";
            mongo.updateFirst(Query.query(Criteria.where("_id").is(entry.getString("_id"))),
                    new Update().set("lastError", error), "pendingImports");
            entry.put("lastError", error);
        } catch (RuntimeException exception) {
            log.error("Pending import retry failed: {}", id, exception);
            String error = "No se pudo completar la importación; revisa las rutas, los archivos y el log";
            mongo.updateFirst(Query.query(Criteria.where("_id").is(entry.getString("_id"))), new Update().set("lastError", error), "pendingImports");
            entry.put("lastError", error);
        }
        return status(entry);
    }

    private void validatePath(Path path, Path root) {
        root = root.toAbsolutePath().normalize();
        if (path.equals(root) || !path.startsWith(root)) throw new IllegalStateException("Unsafe pending import path");
        for (Path current = path; current != null; current = current.getParent()) {
            if (Files.isSymbolicLink(current)) throw new IllegalStateException("Symbolic link in pending import path");
        }
    }
    public void register(EpubFileAddedEvent event) {
        mongo.save(new Document("_id", "book:" + event.getBookId()).append("bookId", event.getBookId())
                .append("source", event.getSourcePath().toAbsolutePath().normalize().toString())
                .append("target", event.getTargetPath().toAbsolutePath().normalize().toString())
                .append("authorImage", event.getAuthorImage()).append("createdAt", new java.util.Date()), "pendingImports");
    }
    public boolean done(String id, String task) {
        return mongo.findById("book:" + id + ":" + task, Document.class, "pendingImportTasks") != null;
    }
    public void complete(String id, String task) {
        mongo.save(new Document("_id", "book:" + id + ":" + task).append("completedAt", new java.util.Date()), "pendingImportTasks");
    }
    public void fileComplete(Path source) {
        Document entry = mongo.findOne(Query.query(Criteria.where("source").is(source.toAbsolutePath().normalize().toString()))
                        .with(org.springframework.data.domain.Sort.by("createdAt").descending()),
                Document.class, "pendingImports");
        if (entry != null) complete(entry.getString("bookId"), "fileDone");
    }
    @EventListener(ApplicationReadyEvent.class)
    public void resume() {
        var query = new Query();
        for (Document entry : mongo.find(query, Document.class, "pendingImports")) {
            try {
                String id = entry.getString("bookId");
                if (done(id, "fileDone") && done(id, "authorsDone") && done(id, "tagsDone")) continue;
                retry(id);
            } catch (RuntimeException exception) { log.error("Pending import requires attention: {}", entry.getString("bookId"), exception); }
        }
    }
}
