package com.martinia.indigo.config;

import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@Configuration
public class PersistenceConfigTest {

	@Container
	private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10")).withExposedPorts(27017);

	static {
		MONGO_DB_CONTAINER.start();
		var mappedPort = MONGO_DB_CONTAINER.getMappedPort(27017);
		System.setProperty("mongodb.container.port", String.valueOf(mappedPort));
	}

}
