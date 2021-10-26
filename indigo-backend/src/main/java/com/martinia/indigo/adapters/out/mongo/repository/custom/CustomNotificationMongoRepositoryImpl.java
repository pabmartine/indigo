package com.martinia.indigo.adapters.out.mongo.repository.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.BsonNull;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.martinia.indigo.adapters.out.mongo.entities.BookMongoEntity;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;

public class CustomNotificationMongoRepositoryImpl implements CustomNotificationMongoRepository {

	@Autowired
	MongoTemplate mongoTemplate;

	@Override
	public List<BookMongoEntity> getRecommendations(String user, int num) {

		List<BookMongoEntity> ret = new ArrayList<>();

		CodecRegistry pojoCodecRegistry = org.bson.codecs.configuration.CodecRegistries.fromRegistries(
				MongoClientSettings.getDefaultCodecRegistry(),
				org.bson.codecs.configuration.CodecRegistries.fromProviders(PojoCodecProvider.builder()
						.automatic(true)
						.build()));

		MongoCollection<Document> collection = mongoTemplate.getCollection("notifications")
				.withCodecRegistry(pojoCodecRegistry);

		AggregateIterable<BookMongoEntity> notifications = collection
				.aggregate(Arrays
						.asList(new Document("$match", new Document("user", "admin")), new Document("$limit", num),
								new Document("$lookup", new Document("from", "books").append("localField", "book")
										.append("foreignField", "path")
										.append("as", "typeCategory")),
								new Document("$match",
										new Document("typeCategory.recommendations",
												new Document("$ne", new BsonNull()))),
								new Document("$project",
										new Document("typeCategory.recommendations", 1L).append("_id", 0L)),
								new Document("$unwind", new Document("path", "$typeCategory")),
								new Document("$unwind", new Document("path", "$typeCategory.recommendations")),
								new Document("$limit", num),
								new Document("$project",
										new Document("_id",
												new Document("$toObjectId", "$typeCategory.recommendations"))),
								new Document("$group",
										new Document("_id", "$_id").append("count", new Document("$sum", 1L))),
								new Document("$sort", new Document("count",
										-1L)),
								new Document("$lookup", new Document("from", "books").append("localField", "_id")
										.append("foreignField", "_id")
										.append("as", "book")),
								new Document("$replaceRoot",
										new Document("newRoot", new Document("$mergeObjects",
												Arrays.asList(new Document("$arrayElemAt", Arrays.asList("$book", 0L)),
														"$$ROOT")))),
								new Document("$unset", Arrays.asList("book", "count"))),
						BookMongoEntity.class);

		notifications.iterator()
				.forEachRemaining(ret::add);

		return ret;
	}

}
