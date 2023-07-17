//package com.martinia.indigo.config;
//
//import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
//import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
//import org.springframework.context.annotation.Configuration;
//import org.testcontainers.containers.MongoDBContainer;
//import org.testcontainers.utility.DockerImageName;
//
//@Configuration
//@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
//public class PersistenceConfigTest {
//
//	private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"));
//
//	static {
//		MONGO_DB_CONTAINER.start();
//	}
//
//}
