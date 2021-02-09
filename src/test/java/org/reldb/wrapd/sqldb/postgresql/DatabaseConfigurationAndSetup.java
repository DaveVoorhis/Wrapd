package org.reldb.wrapd.sqldb.postgresql;

import org.reldb.toolbox.configuration.Configuration;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.exceptions.ExceptionFatal;
import org.reldb.wrapd.sqldb.Customisations;
import org.reldb.wrapd.sqldb.Database;

import java.io.IOException;
import java.sql.SQLException;

public class DatabaseConfigurationAndSetup {

	private static final String baseDir = "./_TestData/PostgreSQL";

	public static void databaseTeardown(String prompt) throws SQLException, IOException {
		var database = getDatabase(prompt);
		databaseTeardown(prompt, database);
		Directory.rmAll(DatabaseConfigurationAndSetup.getCodeDirectory());
	}

	private static void dropTable(Database database, String prompt, String tableName) {
		try {
			database.updateAll("DROP TABLE " + tableName);
		} catch (SQLException se) {
			System.out.println(prompt + " ERROR: " + se);
		}		
	}
	
	public static void databaseTeardown(String prompt, Database database) {
		dropTable(database, prompt, "$$version");
		dropTable(database, prompt, "$$tester");
	}

	public static void databaseCreate(String prompt, Database database) throws SQLException {
		database.transact(xact -> {
			xact.updateAll("CREATE TABLE $$version (user_db_version INTEGER, framework_db_version INTEGER);");
			xact.updateAll("INSERT INTO $$version VALUES (0, 0);");
			xact.updateAll("CREATE TABLE $$tester (x INTEGER, y INTEGER, PRIMARY KEY (x, y));");
			return true;
		});
	}

	public static Database getDatabase(String prompt) throws SQLException, IOException {
		Configuration.setLocation(baseDir);

		Configuration.register(PostgreSQLConfiguration.class);

		// settings should match PostgreSQL configuration in docker-compose.yml
		String dbHost = "localhost";
		String dbDatabase = "wrapd_testdb";
		String dbUser = "user";
		String dbPassword = "password";
		String dbTablenamePrefix = "Wrapd_";

		Database database;
		
		String url = "jdbc:postgresql://" + dbHost + "/" + dbDatabase;
		try {
			database = new Database(url, dbUser, dbPassword, dbTablenamePrefix, null);
		} catch (IOException e) {
			throw new SQLException(prompt + " Database connection failed. Check the configuration. Error is: " + e);
		}
		
		return database;
	}

	public static String getBaseDirectory() {
		return baseDir;
	}
	
	public static String getCodeDirectory() {
		return getBaseDirectory() + "/code";
	}

	public static void ensureTestDirectoryExists() {
		if (!Directory.chkmkdir(baseDir))
			throw new ExceptionFatal("DatabaseConfigurationAndSetup: Unable to create directory for test: " + baseDir);
	}
}
