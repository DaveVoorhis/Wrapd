package org.reldb.wrapd.database;

import java.io.IOException;
import java.sql.SQLException;

import org.reldb.toolbox.configuration.Configuration;
import org.reldb.toolbox.strings.Str;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.exceptions.ExceptionFatal;
import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.sqldb.sqlite.SQLiteConfiguration;
import org.reldb.wrapd.sqldb.sqlite.SQLiteCustomisations;

import static org.reldb.wrapd.il8n.Strings.ErrUnableToCreate1;

public class DatabaseConfigurationAndSetup {

	private static final String baseDir = "./_TestData";

	public static void databaseTeardown(String prompt) throws SQLException, IOException {
		var database = getSQLiteDatabase(prompt);
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
		database.transact(xact -> {
			xact.updateAll("CREATE TABLE $$version (user_db_version INTEGER, framework_db_version INTEGER);");
			xact.updateAll("INSERT INTO $$version VALUES (0, 0);");
			xact.updateAll("CREATE TABLE $$tester (x INTEGER, y INTEGER, PRIMARY KEY (x, y));");
			return true;
		});
	}

	public static Database getSQLiteDatabase(String prompt) throws SQLException, IOException {
		Configuration.setLocation(baseDir);
		
		Configuration.register(SQLiteConfiguration.class);
		
		String dbDatabase = Database.emptyToNull(Configuration.getValue(SQLiteConfiguration.class.getName(), SQLiteConfiguration.DATABASE_NAME));
		String dbTablenamePrefix = Database.nullTo(Configuration.getValue(SQLiteConfiguration.class.getName(), SQLiteConfiguration.DATABASE_TABLENAME_PREFIX), "Wrapd_");

		if (dbDatabase == null)
			throw new SQLException("[TSET] Please specify a database name in the configuration.");

		Database database;
		
		String url = "jdbc:sqlite:" + Configuration.getLocation() + dbDatabase;
		try {
			database = new Database(url, dbTablenamePrefix, new SQLiteCustomisations());
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

	public static void ensureTestDirectoryExists() {
		if (!Directory.chkmkdir(baseDir))
			throw new ExceptionFatal(Str.ing(ErrUnableToCreate1, baseDir));
	}
}
