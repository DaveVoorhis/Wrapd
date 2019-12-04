package org.reldb.wrapd.tests.database.teardown;

import java.io.IOException;
import java.sql.SQLException;

import org.reldb.wrapd.tests.database.shared.DatabaseConfigurationAndSetup;

import org.junit.jupiter.api.Test;

public class TestPostgreSQL {
		
	@Test
	public void teardown() throws SQLException, IOException {
		DatabaseConfigurationAndSetup.databaseTeardown("[TRDN]");
	}
}
