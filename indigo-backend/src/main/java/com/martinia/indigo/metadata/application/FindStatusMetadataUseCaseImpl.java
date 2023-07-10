package com.martinia.indigo.metadata.application;

import com.martinia.indigo.metadata.application.common.BaseMetadataUseCaseImpl;
import com.martinia.indigo.metadata.domain.ports.usecases.FindStatusMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class FindStatusMetadataUseCaseImpl extends BaseMetadataUseCaseImpl implements FindStatusMetadataUseCase {

	@Override
	public Map<String, Object> getStatus() {
		Map<String, Object> data = new HashMap<>();
		data.put("type", metadataSingleton.getType());
		data.put("entity", metadataSingleton.getEntity());
		data.put("status", metadataSingleton.isRunning());
		data.put("current", metadataSingleton.getCurrent());
		data.put("total", metadataSingleton.getTotal());
		data.put("message", metadataSingleton.getMessage());
		return data;
	}

}
