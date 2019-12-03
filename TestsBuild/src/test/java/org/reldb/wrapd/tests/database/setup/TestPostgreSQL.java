package org.reldb.wrapd.tests.database.setup;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.sql.SQLException;

import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.sqldb.Query;
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
		database = DatabaseConfigurationAndSetup.getPostgreSQLDatabase();
		setupCompleted = true;
	}
	
	@Test
	public void testCreateAndInsert() throws SQLException {
		assertTrue(setupCompleted);
		database.new Transaction(connection -> {
			database.updateAll(connection, "CREATE TABLE $$Version (user_db_version INTEGER, framework_db_version INTEGER);");
			database.updateAll(connection, "INSERT INTO $$Version VALUES (0, 0);");
			return true;
		});
	}
	
	@Test
	public void testCreateTupleType() throws SQLException {
		assertTrue(setupCompleted);
		ResultSetToTuple.destroyTuple(DatabaseConfigurationAndSetup.getCodeDirectory(), "TestSelect");
		database.new Transaction(connection -> {
			database.updateAll(connection, "CREATE TABLE $$tester (x INTEGER, y INTEGER);");
			for (int i = 0; i < 20; i++) {
				database.update(connection, "INSERT INTO $$tester VALUES (?, ?);", i, i * 10);
			}
			Query.createTupleFromQueryAll(database, connection, DatabaseConfigurationAndSetup.getCodeDirectory(), "TestSelect", "SELECT * FROM $$tester");
			return true;
		});
	}

}
