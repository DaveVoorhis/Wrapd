package org.reldb.wrapd.tests.database.setup;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;

import org.reldb.wrapd.compiler.ForeignCompilerJava.CompilationResults;
import org.reldb.wrapd.db.Database;
import org.reldb.wrapd.db.ResultSetToTuple;
import org.reldb.wrapd.tests.database.shared.DatabaseConfigurationAndSetup;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestPostgreSQL {

	private static Database database;
	private static boolean setupCompleted;
	
	@BeforeAll
	public static void setup() throws SQLException {
		setupCompleted = false;
		System.out.println("Executing TestPostgreSQL setup.");
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
			database.queryAll(connection, "SELECT * FROM $$tester", result -> {
				CompilationResults makeTupleResult = null;
				try {
					makeTupleResult = ResultSetToTuple.createTuple(DatabaseConfigurationAndSetup.getCodeDirectory(), "TestSelect", result);
				} catch (Exception e) {
					System.out.println("Query failed: " + e);
					e.printStackTrace();
					assertTrue(false);
				}
				if (!makeTupleResult.compiled) {
					System.out.println("ERROR: " + makeTupleResult.compilerMessages);
					assertTrue(false);
				}
				return makeTupleResult;
			});
			return true;
		});
	}

}
