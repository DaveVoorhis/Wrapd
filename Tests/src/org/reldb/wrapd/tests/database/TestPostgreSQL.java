package org.reldb.wrapd.tests.database;

import java.io.IOException;
import java.sql.SQLException;

import org.reldb.wrapd.configuration.Configuration;
import org.reldb.wrapd.db.Database;
import org.reldb.wrapd.exceptions.ExceptionFatal;
import org.reldb.wrapd.version.VersionProxy;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestPostgreSQL {

	private static Database database;
	private static boolean setupCompleted;
	
	@BeforeClass
	public static void setup() {
		setupCompleted = false;

		System.out.println("Executing TestPostgreSQL setup.");
		System.out.println("If you see 'New configuration file WrapdConfiguration.xml written', the tests will fail and");
		System.out.println("you'll have to configure database access in WrapdConfiguration.xml, then re-run the tests.");
		System.out.println();
		
		String dbServer = Database.nullTo(Configuration.getValue(Configuration.DATABASE_SERVER), "localhost");
		String dbDatabase = Database.emptyToNull(Configuration.getValue(Configuration.DATABASE_NAME));
		String dbUser = Database.emptyToNull(Configuration.getValue(Configuration.DATABASE_USER));
		String dbPasswd = Database.emptyToNull(Configuration.getValue(Configuration.DATABASE_PASSWORD));
		String dbPort = Database.emptyToNull(Configuration.getValue(Configuration.DATABASE_NONSTANDARD_PORT));
		String dbTablenamePrefix = Database.nullTo(Configuration.getValue(Configuration.DATABASE_TABLENAME_PREFIX), 
				VersionProxy.getVersion().getInternalProductName() + "_");
		
		if (dbDatabase == null) {
			System.out.println("Please specify a database name in the configuration.");
			return;
		}
		
		if (dbPort != null)
			dbServer += ":" + dbPort;
	
		String url = "jdbc:postgresql://" + dbServer + "/" + dbDatabase;
		try {
			database = new Database(url, dbUser, dbPasswd, dbTablenamePrefix);
		} catch (IOException e) {
			System.out.println("Database connection failed. Check the configuration.");
			e.printStackTrace();
		}
		
		setupCompleted = true;
	}

	@Test
	public void test01() throws SQLException {
		if (!setupCompleted)
			throw new ExceptionFatal("Database test setup failed.");
		
		database.new Transaction(connection -> {
			database.updateAll(connection, "CREATE TABLE $$Version (user_db_version INTEGER, framework_db_version INTEGER);");
			database.updateAll(connection, "INSERT INTO $$Version VALUES (0, 0);");
			return true;
		});
		
	}
	
	@AfterClass
	public static void teardown() {
		if (!setupCompleted)
			return;
		
		try {
			database.new Transaction(connection -> {
				database.updateAll(connection, "DROP TABLE $$Version;");
				return true;
			});
		} catch (SQLException e) {
			System.out.println("Database teardown failed.");
			e.printStackTrace();
		}
	}
}
