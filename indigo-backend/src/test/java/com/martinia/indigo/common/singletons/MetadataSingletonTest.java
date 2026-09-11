package com.martinia.indigo.common.singletons;

import com.martinia.indigo.metadata.domain.model.MetadataItemResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetadataSingletonTest {

	@Test
	void ignoresResultsFromAReplacedRunAndKeepsOutcomeCounters() {
		MetadataSingleton status = new MetadataSingleton();
		long oldRun = status.start("FULL", "BOOKS");
		status.setTotal(2);
		long currentRun = status.start("FULL", "AUTHORS");
		status.setTotal(2);

		status.record(oldRun, MetadataItemResult.FOUND);
		status.record(currentRun, MetadataItemResult.FOUND);
		status.record(currentRun, MetadataItemResult.NOT_FOUND);

		assertEquals(2, status.getCurrent());
		assertEquals(1, status.getFound());
		assertEquals(1, status.getNotFound());
		assertFalse(status.isRunning());
		assertFalse(status.isActive(oldRun));
		assertTrue(status.getRuns().containsKey("FULL:AUTHORS"));
	}
}
