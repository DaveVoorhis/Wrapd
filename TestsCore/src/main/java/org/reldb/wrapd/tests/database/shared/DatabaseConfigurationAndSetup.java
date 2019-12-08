package org.reldb.wrapd.tests.database.shared;

import java.io.IOException;
import java.sql.SQLException;

import org.reldb.toolbox.configuration.Configuration;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.sqldb.postgresql.PostgreSQLConfiguration;

public class DatabaseConfigurationAndSetup {

	private static String baseDir = "../_TestData";
	
	public static void databaseTeardown(String prompt) throws SQLException, IOException {
		var database = getPostgreSQLDatabase(prompt);
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

	public static void databaseCreate(String string, Database database) throws SQLException {
		database.transact(conn -> {
			database.updateAll(conn, "CREATE TABLE $$version (user_db_version INTEGER, framework_db_version INTEGER);");
			database.updateAll(conn, "INSERT INTO $$version VALUES (0, 0);");
			database.updateAll(conn, "CREATE TABLE $$tester (x INTEGER, y INTEGER, PRIMARY KEY (x, y));");
			return true;
		});
	}

	public static Database getPostgreSQLDatabase(String prompt) throws SQLException, IOException {
		
		System.out.println(prompt);
		System.out.println(prompt + " If you see 'New configuration file ../TestData/Configuration.xml written', the tests will fail and");
		System.out.println(prompt + " you'll have to configure database access in _TestData/Configuration.xml, then re-run the tests.");
		System.out.println(prompt);
		
		Configuration.setLocation(baseDir);
		
		Configuration.register(PostgreSQLConfiguration.class);
		
		String dbServer = Database.nullTo(Configuration.getValue(PostgreSQLConfiguration.class.getName(), PostgreSQLConfiguration.DATABASE_SERVER), "localhost");
		String dbDatabase = Database.emptyToNull(Configuration.getValue(PostgreSQLConfiguration.class.getName(), PostgreSQLConfiguration.DATABASE_NAME));
		String dbUser = Database.emptyToNull(Configuration.getValue(PostgreSQLConfiguration.class.getName(), PostgreSQLConfiguration.DATABASE_USER));
		String dbPasswd = Database.emptyToNull(Configuration.getValue(PostgreSQLConfiguration.class.getName(), PostgreSQLConfiguration.DATABASE_PASSWORD));
		String dbPort = Database.emptyToNull(Configuration.getValue(PostgreSQLConfiguration.class.getName(), PostgreSQLConfiguration.DATABASE_NONSTANDARD_PORT));
		String dbTablenamePrefix = Database.nullTo(Configuration.getValue(PostgreSQLConfiguration.class.getName(), PostgreSQLConfiguration.DATABASE_TABLENAME_PREFIX), "Wrapd_");
		
		if (dbDatabase == null)
			throw new SQLException("[TSET] Please specify a database name in the configuration.");
		
		if (dbPort != null)
			dbServer += ":" + dbPort;
	
		Database database;
		
		String url = "jdbc:postgresql://" + dbServer + "/" + dbDatabase;
		try {
			database = new Database(url, dbUser, dbPasswd, dbTablenamePrefix);
		} catch (IOException e) {
			throw new SQLException("[TSET] Database connection failed. Check the configuration. Error is: " + e);
		}
		
		return database;
	}

	public static String getBaseDirectory() {
		return baseDir;
	}
	
	public static String getCodeDirectory() {
		return getBaseDirectory() + "/code";
	}
	
}
