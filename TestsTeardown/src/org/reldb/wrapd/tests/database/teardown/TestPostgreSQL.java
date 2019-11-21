package org.reldb.wrapd.tests.database.teardown;

import java.io.IOException;
import java.sql.SQLException;

import org.reldb.toolbox.configuration.Configuration;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.db.Database;
import org.reldb.wrapd.db.postgresql.WrapDBConfiguration;
import org.reldb.wrapd.version.VersionProxy;

import org.junit.jupiter.api.Test;

public class TestPostgreSQL {
	
	private static String baseDir = "../_TestData";
	private static String codeDir = baseDir + "/code";
	
	@Test
	public void teardown() {		
		Configuration.setLocation(baseDir);
		
		String dbServer = Database.nullTo(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_SERVER), "localhost");
		String dbDatabase = Database.emptyToNull(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_NAME));
		String dbUser = Database.emptyToNull(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_USER));
		String dbPasswd = Database.emptyToNull(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_PASSWORD));
		String dbPort = Database.emptyToNull(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_NONSTANDARD_PORT));
		String dbTablenamePrefix = Database.nullTo(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_TABLENAME_PREFIX), 
				VersionProxy.getVersion().getInternalProductName() + "_");
		
		if (dbDatabase == null) {
			System.out.println("Please specify a database name in the configuration.");
			return;
		}
		
		if (dbPort != null)
			dbServer += ":" + dbPort;
	
		Database database;
		
		String url = "jdbc:postgresql://" + dbServer + "/" + dbDatabase;
		try {
			database = new Database(url, dbUser, dbPasswd, dbTablenamePrefix);
		} catch (IOException e) {
			System.out.println("Database connection failed. Check the configuration.");
			return;
		}
		
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
		
		Directory.rmAll(codeDir);
	}
}
