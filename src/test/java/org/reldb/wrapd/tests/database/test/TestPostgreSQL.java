package org.reldb.wrapd.tests.database.test;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.tests.database.shared.DatabaseConfigurationAndSetup;

import org.reldb.wrapd.tuples.generated.*;

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

public class TestPostgreSQL {

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
	
}