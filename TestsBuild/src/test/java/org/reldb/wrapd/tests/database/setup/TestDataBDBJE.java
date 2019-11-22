package org.reldb.wrapd.tests.database.setup;

import org.junit.jupiter.api.BeforeAll;
import org.reldb.wrapd.data.bdbje.BDBJEEnvironment;

public class TestDataBDBJE {
	
	private final static String testDir = "../_TestData";
	
	@BeforeAll
	public static void setup() {
		BDBJEEnvironment.purge(testDir);
	}

}
