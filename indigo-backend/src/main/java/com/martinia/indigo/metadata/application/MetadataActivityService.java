package com.martinia.indigo.metadata.application;

import com.martinia.indigo.author.infrastructure.mongo.entities.AuthorMongoEntity;
import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import jakarta.annotation.Resource;
import java.util.*;
import java.util.function.Supplier;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MetadataActivityService {
    @org.springframework.beans.factory.annotation.Autowired private MongoTemplate mongo;
    private static final String HISTORY = "metadataHistory";
    private static final String ITEMS = "metadataItems";

    @jakarta.annotation.PostConstruct
    void recoverInterruptedItems() {
        mongo.updateMulti(Query.query(Criteria.where("status").is("RUNNING")), new Update()
                .set("status", "ERROR").set("error", "Interrupted by application restart; retry available"), ITEMS);
    }
    private static final Map<String, List<String>> FIELDS = Map.of(
            "BOOKS", List.of("rating", "ratingAverage", "ratingsCount", "ratingDistribution", "ratingProvider",
                    "ratingUpdatedAt", "provider", "openLibraryWorkId", "openLibraryEditionId", "metadataMatchStatus",
                    "metadataMatchConfidence", "lastMetadataSync"),
            "AUTHORS", List.of("description", "image", "provider", "metadataSources", "lastMetadataSync"),
            "REVIEWS", List.of("reviews", "lastReviewsMetadataSync", "reviewsMetadataStatus", "reviewsMetadataError"));

    public synchronized MetadataItemResult track(String type, String id, String lang, Supplier<MetadataItemResult> action) {
        validate(type);
        Document before = snapshot(type, id);
        if (before == null) return action.get();
        Document lock = mongo.findById(type + ":" + id, Document.class, "metadataLocks");
        if (lock != null && Boolean.TRUE.equals(lock.getBoolean("locked"))) return MetadataItemResult.SKIPPED;
        String historyId = UUID.randomUUID().toString();
        Document item = new Document("_id", type + ":" + id).append("entityId", id).append("type", type)
                .append("lang", lang).append("label", before.getOrDefault("title", before.get("name")))
                .append("status", "RUNNING").append("updatedAt", new Date());
        mongo.save(item, ITEMS);
        MetadataItemResult result;
        String error = null;
        List<Document> diagnostics;
        ProviderDiagnostics.begin();
        try { result = action.get(); }
        catch (RuntimeException exception) {
            result = MetadataItemResult.ERROR;
            error = ProviderDiagnostics.record("INDIGO", "Procesar metadatos", exception);
        }
        finally { diagnostics = ProviderDiagnostics.finish(); }
        if (result == null) result = MetadataItemResult.ERROR;
        Document after = snapshot(type, id);
        List<Document> changes = new ArrayList<>();
        if (after != null) for (String field : FIELDS.get(type)) {
            if (!Objects.equals(before.get(field), after.get(field))) changes.add(new Document("field", field)
                    .append("before", before.get(field)).append("after", after.get(field))
                    .append("provider", fieldProvider(type, field, after)));
        }
        if (error == null && !diagnostics.isEmpty()) error = diagnostics.stream()
                .map(detail -> detail.getString("provider") + " — " + detail.getString("operation") + ": " + detail.getString("message"))
                .collect(java.util.stream.Collectors.joining("; "));
        if (error == null && result == MetadataItemResult.ERROR) error = after == null ? "La entidad ya no existe"
                : "No se pudieron obtener metadatos. Revisa los proveedores habilitados y el log.";
        mongo.save(new Document("_id", historyId).append("entityId", id).append("type", type)
                .append("label", item.get("label")).append("status", result.name()).append("error", error)
                .append("createdAt", new Date()).append("changes", changes).append("diagnostics", diagnostics), HISTORY);
        item.append("status", result.name()).append("error", error).append("diagnostics", diagnostics).append("updatedAt", new Date());
        mongo.save(item, ITEMS);
        return result;
    }

    public List<Document> items() {
        var items = mongo.find(new Query().with(Sort.by(Sort.Direction.DESC, "updatedAt")).limit(100), Document.class, ITEMS);
        for (Document item : items) {
            Document lock = mongo.findById(item.getString("_id"), Document.class, "metadataLocks");
            item.append("locked", lock != null && Boolean.TRUE.equals(lock.getBoolean("locked")));
        }
        return items;
    }
    public List<Document> history() { return mongo.find(new Query().with(Sort.by(Sort.Direction.DESC, "createdAt")).limit(100), Document.class, HISTORY); }
    public Document item(String key) {
        Document item = mongo.findById(key, Document.class, ITEMS);
        if (item == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return item;
    }
    public synchronized void lock(String type, String id, boolean locked) {
        validate(type);
        Document entity = snapshot(type, id);
        if (entity == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        mongo.save(new Document("_id", type + ":" + id).append("locked", locked), "metadataLocks");
        mongo.upsert(Query.query(Criteria.where("_id").is(type + ":" + id)), new Update()
                .set("entityId", id).set("type", type).set("label", entity.getOrDefault("title", entity.get("name")))
                .set("updatedAt", new Date()).setOnInsert("status", "SKIPPED"), ITEMS);
    }

    public boolean isLocked(String type, String id) {
        Document lock = mongo.findById(type + ":" + id, Document.class, "metadataLocks");
        return lock != null && Boolean.TRUE.equals(lock.getBoolean("locked"));
    }
    @org.springframework.transaction.annotation.Transactional
    public synchronized void undo(String historyId) {
        Document entry = mongo.findById(historyId, Document.class, HISTORY);
        if (entry == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (Boolean.TRUE.equals(entry.getBoolean("undone"))) throw new ResponseStatusException(HttpStatus.CONFLICT, "Already undone");
        String type = entry.getString("type");
        Document current = snapshot(type, entry.getString("entityId"));
        if (current == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Criteria criteria = Criteria.where("_id").is(current.get("_id"));
        Update update = new Update();
        List<Document> changes = entry.getList("changes", Document.class);
        if (changes.isEmpty()) throw new ResponseStatusException(HttpStatus.CONFLICT, "No changes to undo");
        for (Document change : changes) {
            String field = change.getString("field");
            if (!FIELDS.get(type).contains(field)) throw new ResponseStatusException(HttpStatus.CONFLICT);
            criteria = criteria.and(field).is(change.get("after"));
            update.set(field, change.get("before"));
        }
        if (mongo.updateFirst(new Query(criteria), update, collection(type)).getMatchedCount() != 1)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Metadata changed since this operation; not overwritten");
        mongo.updateFirst(Query.query(Criteria.where("_id").is(historyId)), new Update().set("undone", true), HISTORY);
        lock(type, entry.getString("entityId"), true);
    }
    private Document snapshot(String type, String id) {
        Object entity = "AUTHORS".equals(type) ? mongo.findById(id, AuthorMongoEntity.class) : mongo.findById(id, BookMongoEntity.class);
        if (entity == null) return null;
        Document doc = new Document();
        mongo.getConverter().write(entity, doc);
        return doc;
    }
    private String collection(String type) { return "AUTHORS".equals(type) ? "authors" : "books"; }
    private Object fieldProvider(String type, String field, Document after) {
        if ("AUTHORS".equals(type) && after.get("metadataSources") instanceof Map<?, ?> sources
                && sources.containsKey(field)) return sources.get(field);
        if (field.startsWith("openLibrary")) return "OPEN_LIBRARY";
        if (List.of("rating", "ratingAverage", "ratingsCount", "ratingDistribution", "ratingProvider").contains(field))
            return after.getOrDefault("ratingProvider", after.get("provider"));
        if ("reviews".equals(field)) return "Proveedor indicado en cada reseña";
        return "INDIGO";
    }
    private void validate(String type) { if (!FIELDS.containsKey(type)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid entity type"); }
}
