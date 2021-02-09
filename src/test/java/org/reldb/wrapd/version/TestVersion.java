package org.reldb.wrapd.version;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestVersion {
	
	@Test 
	public void testGetVersion() {
		var version = Version.getVersionString();
		System.out.println("[TEST] === Version: " + version);
		assertNotNull(version);
		assertFalse(version.startsWith("?"));
	}

}
