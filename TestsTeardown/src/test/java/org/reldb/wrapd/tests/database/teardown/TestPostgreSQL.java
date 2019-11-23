package org.reldb.wrapd.tests.database.teardown;

import java.sql.SQLException;

import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.tests.database.shared.DatabaseConfigurationAndSetup;

import org.junit.jupiter.api.Test;

public class TestPostgreSQL {
		
	@Test
	public void teardown() throws SQLException {		
		var database = DatabaseConfigurationAndSetup.getPostgreSQLDatabase();
		try {
			database.new Transaction(connection -> {
				try {
					database.updateAll(connection, "DROP TABLE $$Version;");
				} catch (SQLException se) {
					System.out.println("ERROR: " + se);
				}
				try {
					database.updateAll(connection, "DROP TABLE $$tester;");
				} catch (SQLException se) {
					System.out.println("ERROR: " + se);
				}
				return true;
			});
		} catch (SQLException e) {
			System.out.println("Database teardown failed.");
			e.printStackTrace();
		}
		
		Directory.rmAll(DatabaseConfigurationAndSetup.getCodeDirectory());
	}
}
