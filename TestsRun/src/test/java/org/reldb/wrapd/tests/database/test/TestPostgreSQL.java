package org.reldb.wrapd.tests.database.test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.reldb.wrapd.db.ResultSetToTuple;
import org.reldb.wrapd.tests.database.shared.DatabaseConfigurationAndSetup;
import org.reldb.wrapd.tuples.generated.TestSelect;

public class TestPostgreSQL {
		
	@Test
	public void testQueryToStream01() throws SQLException, IOException {
		var database = DatabaseConfigurationAndSetup.getPostgreSQLDatabase();
		database.new Transaction(connection -> {
			database.queryAll(connection, "SELECT * FROM $$tester", result -> {
				try {
					var testSelectStream = ResultSetToTuple.toStream(result, TestSelect.class);
					testSelectStream.forEach(tuple -> System.out.println(tuple.toString()));
				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException | NoSuchFieldException e) {
					e.printStackTrace();
				}
				return null;
			});
			return true;
		});
		
	}
	
}