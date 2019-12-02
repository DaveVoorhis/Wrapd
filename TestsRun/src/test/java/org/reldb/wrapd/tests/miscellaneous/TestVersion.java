package org.reldb.wrapd.tests.miscellaneous;

import org.junit.jupiter.api.Test;
import org.reldb.wrapd.version.Version;

public class TestVersion {
	
	@Test 
	public void testGetVersion() {
		System.out.println("[TEST] === Version: " + Version.getVersionString());
	}

}
