package com.martinia.indigo.metadata.application;

import com.martinia.indigo.common.singletons.MetadataSingleton;
import com.martinia.indigo.common.singletons.UploadEpubFilesSingleton;
import com.martinia.indigo.metadata.domain.ports.usecases.FindStatusMetadataUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
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
		data.put("completedAt", metadataSingleton.getCompletedAt());
		data.put("found", metadataSingleton.getFound());
		data.put("notFound", metadataSingleton.getNotFound());
		data.put("skipped", metadataSingleton.getSkipped());
		data.put("errors", metadataSingleton.getErrors());
		data.put("runs", metadataSingleton.getRuns());

		data.put("uploadsTotal", uploadEpubFilesSingleton.getTotal());
		data.put("uploadsCurrent", uploadEpubFilesSingleton.getCurentStatus());
		data.put("uploadsRunning", uploadEpubFilesSingleton.isRunning());
		data.put("uploadsProcessed", uploadEpubFilesSingleton.getProcessedItems());
		data.put("uploadsFailed", uploadEpubFilesSingleton.getFailedItems());
		data.put("uploadsSucceeded", uploadEpubFilesSingleton.getProcessedItems() - uploadEpubFilesSingleton.getFailedItems());
		data.put("uploadsNewBooks", uploadEpubFilesSingleton.getNewBooks());
		data.put("uploadsUpdatedBooks", uploadEpubFilesSingleton.getUpdatedBooks());
		data.put("uploadsMoved", uploadEpubFilesSingleton.getMoved());
		data.put("uploadsDeleted", uploadEpubFilesSingleton.getDeleted());
		return data;
	}

}
