package org.reldb.wrapd.tests.database.setup;

import org.junit.BeforeClass;
import org.reldb.wrapd.data.bdbje.BDBJEEnvironment;

public class TestDataBDBJE {
	
	private final static String testDir = "../_TestData";
	
	@BeforeClass
	public static void setup() {
		BDBJEEnvironment.purge(testDir);
	}

}
