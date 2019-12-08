package org.reldb.wrapd.tests.database.setup;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.sql.SQLException;

import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.sqldb.ResultSetToTuple;
import org.reldb.wrapd.tests.database.shared.DatabaseConfigurationAndSetup;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestPostgreSQL {

	private static Database database;
	private static boolean setupCompleted;
	
	@BeforeAll
	public static void setup() throws SQLException, IOException {
		setupCompleted = false;
		System.out.println("[TSET] Executing TestPostgreSQL setup.");
		database = DatabaseConfigurationAndSetup.getPostgreSQLDatabase("[TSET]");
		setupCompleted = true;
	}
	
	@Test
	public void testCreateAndInsert() throws SQLException {
		assertTrue(setupCompleted);
		DatabaseConfigurationAndSetup.databaseTeardown("[TSET]", database);
		database.new Transaction(connection -> {
			DatabaseConfigurationAndSetup.databaseCreate("[TSET]", database, connection);
			return true;
		});
	}
	
	@Test
	public void testCreateTupleType() throws SQLException {
		assertTrue(setupCompleted);
		final var tupleClassName = "TestSelect";
		ResultSetToTuple.destroyTuple(DatabaseConfigurationAndSetup.getCodeDirectory(), tupleClassName);
		database.createTupleFromQueryAll(DatabaseConfigurationAndSetup.getCodeDirectory(), tupleClassName, "SELECT * FROM $$tester");
	}

}
