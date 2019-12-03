package org.reldb.wrapd.tests.database.test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.sqldb.Query;
import org.reldb.wrapd.sqldb.ResultSetToTuple;
import org.reldb.wrapd.tests.database.shared.DatabaseConfigurationAndSetup;
import org.reldb.wrapd.tuples.Tuple;
import org.reldb.wrapd.tuples.generated.TestSelect;

/**
 * This project references generated code!
 * 
 * If it doesn't compile, it's because the TestsBuild tests need to be run.
 * 
 * I.e., go to TestsBuild and do a <i>mvn test</i>
 * 
 * @author dave
 *
 */

public class TestPostgreSQL {
		
	@Test
	public void testQueryToStream01() throws SQLException, IOException {
		var database = DatabaseConfigurationAndSetup.getPostgreSQLDatabase("[TEST]");
		database.new Transaction(connection -> {		
			Query.queryAll(database, connection, "SELECT * FROM $$tester", TestSelect.class)
				.forEach(tuple -> System.out.println("[TEST] " + tuple.toString()));
			return true;
		});
	}
	
}
