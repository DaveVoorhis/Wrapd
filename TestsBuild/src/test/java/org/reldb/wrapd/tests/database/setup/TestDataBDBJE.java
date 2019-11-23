package org.reldb.wrapd.tests.database.setup;

import org.junit.jupiter.api.BeforeAll;
import org.reldb.wrapd.data.bdbje.BDBJEEnvironment;
import org.reldb.wrapd.tests.database.shared.DatabaseConfigurationAndSetup;

public class TestDataBDBJE {
	
	private final static String testDir = DatabaseConfigurationAndSetup.getBaseDirectory();
	
	@BeforeAll
	public static void setup() {
		BDBJEEnvironment.purge(testDir);
	}

}
