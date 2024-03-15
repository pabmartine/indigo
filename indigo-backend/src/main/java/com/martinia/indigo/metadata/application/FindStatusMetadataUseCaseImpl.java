package com.martinia.indigo.metadata.application;

import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.metadata.domain.ports.usecases.FindStatusMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class FindStatusMetadataUseCaseImpl implements FindStatusMetadataUseCase {

	@Resource
	protected MetadataSingleton metadataSingleton;

	@Resource
	protected UploadEpubFilesSingleton uploadEpubFilesSingleton;

	@Override
	public Map<String, Object> getStatus() {
		Map<String, Object> data = new HashMap<>();
		data.put("type", metadataSingleton.getType());
		data.put("entity", metadataSingleton.getEntity());
		data.put("status", metadataSingleton.isRunning());
		data.put("current", metadataSingleton.getCurrent());
		data.put("total", metadataSingleton.getTotal());
		data.put("message", metadataSingleton.getMessage());

		data.put("uploadsTotal", uploadEpubFilesSingleton.getTotal());
		data.put("uploadsCurrent", uploadEpubFilesSingleton.getCurentStatus());
		return data;
	}

}
