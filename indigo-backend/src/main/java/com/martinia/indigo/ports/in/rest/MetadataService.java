package com.martinia.indigo.ports.in.rest;

import java.util.Map;

public interface MetadataService {

	void initialLoad(String lang);

	Map<String, Object> getStatus();

	void noFilledMetadata(String lang);

	void stop();

}
