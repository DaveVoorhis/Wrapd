package org.reldb.wrapd.tests.database.test;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
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
		
	@Test
	public void testQueryToStream01() throws SQLException, IOException {
		var database = DatabaseConfigurationAndSetup.getPostgreSQLDatabase("[TEST]");
		database.queryAll("SELECT * FROM $$tester", TestSelect.class)
			.forEach(tuple -> System.out.println("[TEST] " + tuple.x + ", " + tuple.y));
	}
	
	@Test
	public void testQueryToStream02() throws SQLException, IOException {
		var database = DatabaseConfigurationAndSetup.getPostgreSQLDatabase("[TEST]");
		database.query("SELECT * FROM $$tester", TestSelect.class)
			.forEach(tuple -> System.out.println("[TEST] " + tuple.x + ", " + tuple.y));
	}
	
	@Test
	public void testQueryToStream03() throws SQLException, IOException {
		var database = DatabaseConfigurationAndSetup.getPostgreSQLDatabase("[TEST]");
		database.query("SELECT * FROM $$tester WHERE x > ? AND x < ?", TestSelect.class, 3, 7)
			.forEach(tuple -> System.out.println("[TEST] " + tuple.x + ", " + tuple.y));
	}
	
	@Test
	public void testInsert01() throws SQLException, IOException {
		var database = DatabaseConfigurationAndSetup.getPostgreSQLDatabase("[TEST]");
		for (int x = 100; x < 200; x++) {
			var tuple = new TestSelect();
			tuple.x = x;
			tuple.y = x * 2;
			tuple.insert(database, "$$tester", tuple);
		}
		database.query("SELECT * FROM $$tester WHERE x >= ?", TestSelect.class, 100)
			.forEach(tuple -> System.out.println("[TEST] " + tuple.toString()));
	}

	@Test
	public void testUpdate01() throws SQLException, IOException {
		var database = DatabaseConfigurationAndSetup.getPostgreSQLDatabase("[TEST]");
		System.out.println("********** primary key is " + database.getKeyColumnNamesFor("$$tester"));
		database.queryAllForUpdate("SELECT * FROM $$tester WHERE x > 3 AND x < 7", TestSelect.class)
			.forEach(tuple -> {
				tuple.x *= 100;
				tuple.y *= 100;
				tuple.update(database, "$$tester", tuple);
			});
		database.query("SELECT * FROM $$tester WHERE x >= ? AND x <= ?", TestSelect.class, 300, 700)
			.forEach(tuple -> System.out.println("[TEST] " + tuple.toString()));
	}
	
	@Test
	public void testUpdate02() throws SQLException, IOException {
		var database = DatabaseConfigurationAndSetup.getPostgreSQLDatabase("[TEST]");
		database.queryForUpdate("SELECT * FROM $$tester WHERE x >= ?", TestSelect.class, 100)
			.forEach(tuple -> {
				if (tuple.x >= 120 && tuple.x <= 130) {
					tuple.x *= 100;
					tuple.y *= 100;
					tuple.update(database, "$$tester", tuple);
				}
			});
		database.query("SELECT * FROM $$tester WHERE x >= ? AND x <= ?", TestSelect.class, 1200, 1300)
			.forEach(tuple -> System.out.println("[TEST] " + tuple.toString()));
	}
	
}
