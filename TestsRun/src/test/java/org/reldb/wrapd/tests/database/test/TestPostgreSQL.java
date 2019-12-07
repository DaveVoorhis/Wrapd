package org.reldb.wrapd.tests.database.test;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.reldb.wrapd.sqldb.Query;
import org.reldb.wrapd.sqldb.Update;
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
		Query.queryAll(database, "SELECT * FROM $$tester", TestSelect.class)
			.forEach(tuple -> System.out.println("[TEST] " + tuple.x + ", " + tuple.y));
	}
	
	@Test
	public void testQueryToStream02() throws SQLException, IOException {
		var database = DatabaseConfigurationAndSetup.getPostgreSQLDatabase("[TEST]");
		Query.query(database, "SELECT * FROM $$tester", TestSelect.class)
			.forEach(tuple -> System.out.println("[TEST] " + tuple.x + ", " + tuple.y));
	}
	
	@Test
	public void testQueryToStream03() throws SQLException, IOException {
		var database = DatabaseConfigurationAndSetup.getPostgreSQLDatabase("[TEST]");
		Query.query(database, "SELECT * FROM $$tester WHERE x > ? AND x < ?", TestSelect.class, 3, 7)
			.forEach(tuple -> System.out.println("[TEST] " + tuple.x + ", " + tuple.y));
	}
	
	@Test
	public void testUpdate01() throws SQLException, IOException {
		var database = DatabaseConfigurationAndSetup.getPostgreSQLDatabase("[TEST]");
		for (int x = 100; x < 200; x++) {
			var tuple = new TestSelect();
			tuple.x = x;
			tuple.y = x * 2;
			database.insert("$$tester", tuple);
		}
		Query.query(database, "SELECT * FROM $$tester WHERE x >= ?", TestSelect.class, 100)
			.forEach(tuple -> System.out.println("[TEST] " + tuple.toString()));
	}
	
}
