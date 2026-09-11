package com.martinia.indigo.metadata.application.reviews;

import com.martinia.indigo.book.infrastructure.mongo.entities.BookMongoEntity;
import com.martinia.indigo.metadata.application.MetadataActivityService;
import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import com.martinia.indigo.metadata.domain.ports.usecases.commands.FindReviewMetadataUseCase;
import jakarta.annotation.*;
import java.util.*;
import java.util.concurrent.*;
import org.bson.Document;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Single-instance durable review worker. Cursor and upper bound avoid loading the library into memory. */
@Service
public class ReviewQueueService {
    private static final String COLLECTION = "reviewQueue";
    private static final ThreadLocal<String> EXECUTING = new ThreadLocal<>();
    @Autowired private MongoTemplate mongo;
    @Autowired private ObjectProvider<FindReviewMetadataUseCase> reviews;
    @Autowired private ObjectProvider<MetadataActivityService> activity;
    private ScheduledExecutorService worker;
    private volatile boolean inFlight;
    @org.springframework.beans.factory.annotation.Value("${metadata.reviews.queue.worker-enabled:true}") private boolean workerEnabled;

    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    void launch() {
        if (!workerEnabled) return;
        worker = Executors.newSingleThreadScheduledExecutor(r -> { Thread t = new Thread(r, "review-queue"); t.setDaemon(true); return t; });
        worker.scheduleWithFixedDelay(() -> { try { tick(); } catch (RuntimeException ex) {
            org.slf4j.LoggerFactory.getLogger(getClass()).error("Review queue iteration failed", ex);
        } }, 5, 1, TimeUnit.SECONDS);
    }
    @PreDestroy void shutdown() { if (worker != null) worker.shutdownNow(); }

    public synchronized Document status() {
        Document job = mongo.findById("active", Document.class, COLLECTION);
        if (job == null) job = new Document("status", "IDLE");
        return job.append("inFlight", inFlight).append("settings", settings());
    }
    public Document settings() {
        Document settings = mongo.findById("settings", Document.class, COLLECTION);
        return settings == null ? new Document("amazon", 30).append("goodreads", 30) : settings;
    }
    public synchronized void configure(int amazon, int goodreads) {
        if (amazon < 15 || goodreads < 15 || amazon > 3600 || goodreads > 3600)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El intervalo debe estar entre 15 y 3600 segundos");
        mongo.save(new Document("_id", "settings").append("amazon", amazon).append("goodreads", goodreads), COLLECTION);
    }
    public synchronized Document start(boolean all, String lang, boolean replace) {
        Document old = mongo.findById("active", Document.class, COLLECTION);
        boolean active = old != null && List.of("RUNNING", "PAUSED").contains(old.getString("status"));
        if ((active || inFlight) && (!all || !replace)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya hay un proceso de reseñas; confirma el reinicio completo o espera a que termine");
        BookMongoEntity last = mongo.findOne(new Query().with(Sort.by(Sort.Direction.DESC, "id")).limit(1), BookMongoEntity.class);
        Document job = new Document("_id", "active").append("runId", UUID.randomUUID().toString())
                .append("status", last == null ? "COMPLETED" : "RUNNING").append("all", all).append("lang", lang)
                .append("upperId", last == null ? null : last.getId()).append("cursor", null)
                .append("processed", 0L).append("errors", 0L).append("createdAt", new Date());
        job.append("total", last == null ? 0 : mongo.count(selection(job), BookMongoEntity.class));
        mongo.save(job, COLLECTION);
        return status();
    }
    public synchronized Document control(String action) {
        Document job = mongo.findById("active", Document.class, COLLECTION);
        if (job == null) throw new ResponseStatusException(HttpStatus.CONFLICT);
        String state = job.getString("status");
        String next = switch (action) {
            case "pause" -> "RUNNING".equals(state) ? "PAUSED" : null;
            case "resume" -> "PAUSED".equals(state) ? "RUNNING" : null;
            case "stop" -> List.of("RUNNING", "PAUSED").contains(state) ? "STOPPED" : null;
            default -> null;
        };
        if (next == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "La acción no es válida para el estado actual");
        mongo.updateFirst(Query.query(Criteria.where("_id").is("active")), new Update().set("status", next).unset("reason"), COLLECTION);
        return status();
    }
    private Query selection(Document job) {
        Criteria id = Criteria.where("id").lte(job.getString("upperId"));
        if (job.getString("cursor") != null) id.gt(job.getString("cursor"));
        Query query = Query.query(id);
        if (!job.getBoolean("all")) query.addCriteria(new Criteria().orOperator(Criteria.where("reviews").is(null), Criteria.where("reviews").size(0)));
        return query;
    }
    void tick() {
        Document job;
        synchronized (this) {
            job = mongo.findById("active", Document.class, COLLECTION);
            if (job == null || !"RUNNING".equals(job.getString("status")) || inFlight) return;
            if (job.getDate("nextAttempt") != null && job.getDate("nextAttempt").after(new Date())) return;
            inFlight = true;
        }
        String run = job.getString("runId");
        EXECUTING.set(run);
        try {
            BookMongoEntity book = mongo.findOne(selection(job).with(Sort.by("id").ascending()).limit(1), BookMongoEntity.class);
            Query current = Query.query(Criteria.where("_id").is("active").and("runId").is(run));
            if (book == null) {
                current.addCriteria(Criteria.where("status").is("RUNNING"));
                mongo.updateFirst(current, new Update().set("status", "COMPLETED").set("completedAt", new Date()), COLLECTION);
                return;
            }
            MetadataItemResult result = activity.getObject().track("REVIEWS", book.getId(), job.getString("lang"),
                    () -> reviews.getObject().find(book.getId(), true, job.getString("lang")));
            synchronized (this) {
                Document now = mongo.findById("active", Document.class, COLLECTION);
                if (!run.equals(now.getString("runId"))) return;
                if (result == MetadataItemResult.ERROR) {
                    if (!"RUNNING".equals(now.getString("status"))) return;
                    Document item = mongo.findById("REVIEWS:" + book.getId(), Document.class, "metadataItems");
                    if (item != null && item.getList("diagnostics", Document.class, List.of()).stream()
                            .anyMatch(d -> "ACCESS_RESTRICTED".equals(d.getString("code")))) {
                        mongo.updateFirst(current, new Update().set("status", "PAUSED").set("reason", "Acceso restringido por el proveedor; revisa el panel de actividad"), COLLECTION);
                        return;
                    }
                    Date retry = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30));
                    for (String provider : List.of("amazon", "goodreads")) {
                        Document restriction = mongo.findById(provider, Document.class, "reviewProviderStates");
                        if (restriction != null && restriction.getDate("blockedUntil") != null && restriction.getDate("blockedUntil").after(retry)) retry = restriction.getDate("blockedUntil");
                    }
                    int attempts = ((Number) now.getOrDefault("attempts", 0)).intValue() + 1;
                    Update update = new Update().set("nextAttempt", retry).inc("errors", 1).set("attempts", attempts);
                    if (attempts >= 3) update.set("cursor", book.getId()).set("attempts", 0).inc("processed", 1).inc("failedBooks", 1);
                    mongo.updateFirst(current, update, COLLECTION);
                } else mongo.updateFirst(current, new Update().set("cursor", book.getId()).set("attempts", 0).unset("nextAttempt").inc("processed", 1), COLLECTION);
            }
        } finally { EXECUTING.remove(); inFlight = false; }
    }
    /** Called before every explicit Goodreads/Amazon HTTP request, including searches and fallbacks. */
    public void awaitPermit(String provider) {
        while (true) {
            synchronized (this) {
                String run = EXECUTING.get();
                if (run != null) {
                    Document job = mongo.findById("active", Document.class, COLLECTION);
                    if (Thread.currentThread().isInterrupted() || job == null || !run.equals(job.getString("runId")) || !"RUNNING".equals(job.getString("status")))
                        throw new CancellationException("Review queue paused, stopped or replaced");
                }
                String key = "request:" + provider;
                Document previous = mongo.findById(key, Document.class, COLLECTION);
                long last = previous == null ? 0 : previous.getDate("at").getTime();
                long interval = ((Number) settings().get(provider)).longValue() * 1000;
                if (System.currentTimeMillis() - last >= interval) {
                    mongo.save(new Document("_id", key).append("at", new Date()), COLLECTION);
                    return;
                }
            }
            try { Thread.sleep(250); } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new CancellationException("Review request interrupted"); }
        }
    }
    public static void rethrowCancellation(Throwable error) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Throwable cause = error; cause != null && visited.add(cause); cause = cause.getCause())
            if (cause instanceof CancellationException cancelled) throw cancelled;
    }
}
