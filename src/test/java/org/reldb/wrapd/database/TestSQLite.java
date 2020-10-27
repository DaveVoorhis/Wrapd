package org.reldb.wrapd.database;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.sqldb.ResultSetToTuple;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Temporarily disabled
 * import org.reldb.wrapd.tuples.generated.*;
 */
// TODO re-enable tests

/**
 * This project references generated code!
 * 
 * If it doesn't compile, it's because the TestsBuild tests need to be run.
 * 
 * I.e., go to TestsBuild and do a <i>mvn test</i> or <i>mvn install</i>.
 * 
 * @author dave
 *
 */

/**
 * Temporarily mostly disabled.
 */
public class TestSQLite {

	private static Database database;
	private static boolean setupCompleted;

	private static final String prompt = "[TSET]";

	@BeforeAll
	public static void setup() throws SQLException, IOException {
		setupCompleted = false;
		System.out.println(prompt + " Executing TestSQLite setup.");
		database = DatabaseConfigurationAndSetup.getSQLiteDatabase(prompt);
		setupCompleted = true;
	}

	@Test
	public void testTeardownAndCreate() throws SQLException {
		assertTrue(setupCompleted);
		DatabaseConfigurationAndSetup.databaseTeardown(prompt, database);
		DatabaseConfigurationAndSetup.databaseCreate(prompt, database);
	}

	@Test
	public void testCreateTupleType() throws SQLException {
		assertTrue(setupCompleted);
		final var tupleClassName = "TestSelect";
		ResultSetToTuple.destroyTuple(DatabaseConfigurationAndSetup.getCodeDirectory(), tupleClassName);
		database.createTupleFromQueryAll(DatabaseConfigurationAndSetup.getCodeDirectory(), tupleClassName, "SELECT * FROM $$tester");
	}

	// TODO re-enable tests
	/**
	private static final String prompt = "[TEST]";

	private static void resetDatabase(Database database) throws SQLException {
		database.updateAll("DELETE FROM $$tester");
		for (int i = 0; i < 20; i++) {
			database.update("INSERT INTO $$tester VALUES (?, ?);", i, i * 10);
		}
	}
	
	@BeforeAll
	public static void setup() throws SQLException, IOException {
		var database = DatabaseConfigurationAndSetup.getPostgreSQLDatabase(prompt);
		resetDatabase(database);
	}
	
	@Test
	public void testQueryToStream01() throws SQLException, IOException {
		var database = DatabaseConfigurationAndSetup.getPostgreSQLDatabase(prompt);
		System.out.println(prompt + " testQueryToStream01");
		database.queryAll("SELECT * FROM $$tester", TestSelect.class)
			.forEach(tuple -> System.out.println("[TEST] " + tuple.x + ", " + tuple.y));
	}
	
	@Test
	public void testQueryToStream02() throws SQLException, IOException {
		var database = DatabaseConfigurationAndSetup.getPostgreSQLDatabase(prompt);
		System.out.println(prompt + " testQueryToStream02");
		database.query("SELECT * FROM $$tester", TestSelect.class)
			.forEach(tuple -> System.out.println("[TEST] " + tuple.x + ", " + tuple.y));
	}
	
	@Test
	public void testQueryToStream03() throws SQLException, IOException {
		var database = DatabaseConfigurationAndSetup.getPostgreSQLDatabase(prompt);
		System.out.println(prompt + " testQueryToStream03");
		database.query("SELECT * FROM $$tester WHERE x > ? AND x < ?", TestSelect.class, 3, 7)
			.forEach(tuple -> System.out.println("[TEST] " + tuple.x + ", " + tuple.y));
	}
	
	@Test
	public void testInsert01() throws SQLException, IOException {
		var database = DatabaseConfigurationAndSetup.getPostgreSQLDatabase(prompt);
		System.out.println(prompt + " testInsert01");
		for (int x = 1000; x < 1010; x++) {
			var tuple = new TestSelect();
			tuple.x = x;
			tuple.y = x * 2;
			tuple.insert(database, "$$tester");
		}
		database.query("SELECT * FROM $$tester WHERE x >= ? AND x < ?", TestSelect.class, 1000, 1010)
			.forEach(tuple -> System.out.println("[TEST] " + tuple.toString()));
	}

	@Test
	public void testUpdate01() throws SQLException, IOException {
		var database = DatabaseConfigurationAndSetup.getPostgreSQLDatabase(prompt);
		System.out.println(prompt + " testUpdate01");
		database.queryAllForUpdate("SELECT * FROM $$tester WHERE x > 3 AND x < 7", TestSelect.class)
			.forEach(tuple -> {
				tuple.x *= 100;
				tuple.y *= 100;
				try {
					tuple.update(database, "$$tester");
				} catch (SQLException e) {
					System.out.println("Update failed.");
				}
			});
		database.query("SELECT * FROM $$tester WHERE x >= ? AND x <= ?", TestSelect.class, 300, 700)
			.forEach(tuple -> System.out.println("[TEST] " + tuple.toString()));
	}
	
	@Test
	public void testUpdate02() throws SQLException, IOException {
		var database = DatabaseConfigurationAndSetup.getPostgreSQLDatabase(prompt);
		System.out.println(prompt + " testUpdate02");
		resetDatabase(database);
		database.queryForUpdate("SELECT * FROM $$tester WHERE x >= ?", TestSelect.class, 10)
			.forEach(tuple -> {
				if (tuple.x >= 12 && tuple.x <= 13) {
					tuple.x *= 100;
					tuple.y *= 100;
					try {
						tuple.update(database, "$$tester");
					} catch (SQLException e) {
						System.out.println("Update failed.");
					}
				}
			});
		database.query("SELECT * FROM $$tester WHERE x >= ? AND x <= ?", TestSelect.class, 1200, 1300)
			.forEach(tuple -> System.out.println("[TEST] " + tuple.toString()));
	}
	*/

	@AfterAll
	public static void teardown() throws SQLException, IOException {
		DatabaseConfigurationAndSetup.databaseTeardown("[TRDN]");
	}

}

