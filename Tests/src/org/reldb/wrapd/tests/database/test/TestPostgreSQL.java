package org.reldb.wrapd.tests.database.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;
import org.reldb.wrapd.configuration.Configuration;
import org.reldb.wrapd.db.Database;
import org.reldb.wrapd.db.ResultSetToTuple;
import org.reldb.wrapd.db.Database.Transaction;
import org.reldb.wrapd.version.VersionProxy;

public class TestPostgreSQL {
	
	private static String codeDir = "./test/code";
	
	@Test
	public void testQueryToStream01() throws SQLException {
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
	
		Database database;
		
		String url = "jdbc:postgresql://" + dbServer + "/" + dbDatabase;
		try {
			database = new Database(url, dbUser, dbPasswd, dbTablenamePrefix);
		} catch (IOException e) {
			System.out.println("Database connection failed. Check the configuration.");
			return;
		}
		
		database.new Transaction(connection -> {
			database.queryAll(connection, "SELECT * FROM $$tester", result -> {
				TestSelect t;
				var testSelectStream = ResultSetToTuple.toStream(result, TestSelect.class);
				return null;
			});
			return true;
		});
		
	}
	
}
