package org.reldb.wrapd.tests.miscellaneous;

import org.junit.jupiter.api.Test;
import org.reldb.wrapd.version.VersionDefault;

public class TestVersion {
	
	@Test 
	public void testGetVersion() {
		var version = new VersionDefault();
		System.out.println("=== Version: " + version.getVersionString());
	}

}
