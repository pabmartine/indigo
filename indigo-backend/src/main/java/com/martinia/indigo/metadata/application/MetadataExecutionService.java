package com.martinia.indigo.metadata.application;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

/** Inspection checkpoints are separate from entity snapshots and provider success timestamps. */
@Service
public class MetadataExecutionService {
    private static final String COLLECTION = "metadataExecutions";
    @Autowired private MongoTemplate mongo;

    public List<String> pending(String process, List<String> eligible) {
        if (eligible.isEmpty()) return eligible;
        Query selection = Query.query(Criteria.where("process").is(process));
        selection.fields().include("entityId");
        Set<String> inspected = new HashSet<>();
        mongo.find(selection, Document.class, COLLECTION)
                .forEach(item -> inspected.add(item.getString("entityId")));
        List<String> pending = eligible.stream().filter(id -> !inspected.contains(id)).toList();
        if (!pending.isEmpty()) return pending;
        // Reset only on the next launch, never when a run is stopped or replaced.
        mongo.remove(Query.query(Criteria.where("process").is(process)), COLLECTION);
        return eligible;
    }

    public void inspected(String process, String entityId) {
        mongo.upsert(Query.query(Criteria.where("_id").is(process + ":" + entityId)),
                new Update().set("process", process).set("entityId", entityId)
                        .set("lastExecution", new Date()), COLLECTION);
    }
}
