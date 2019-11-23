package org.reldb.wrapd.tests.database.shared;

import java.io.IOException;
import java.sql.SQLException;

import org.reldb.toolbox.configuration.Configuration;
import org.reldb.toolbox.configuration.ConfigurationSettings;
import org.reldb.wrapd.db.Database;
import org.reldb.wrapd.db.postgresql.WrapDBConfiguration;
import org.reldb.wrapd.version.VersionProxy;

public class DatabaseConfigurationAndSetup {

	private static String baseDir = "../_TestData";

	public static Database getPostgreSQLDatabase() throws SQLException {
		
		System.out.println("If you see 'New configuration file ../TestData/WrapdConfiguration.xml written', the tests will fail and");
		System.out.println("you'll have to configure database access in _TestData/WrapdConfiguration.xml, then re-run the tests.");
		System.out.println();
		
		Configuration.setLocation(baseDir);
		
		ConfigurationSettings.register(WrapDBConfiguration.class);
		
		String dbServer = Database.nullTo(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_SERVER), "localhost");
		String dbDatabase = Database.emptyToNull(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_NAME));
		String dbUser = Database.emptyToNull(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_USER));
		String dbPasswd = Database.emptyToNull(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_PASSWORD));
		String dbPort = Database.emptyToNull(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_NONSTANDARD_PORT));
		String dbTablenamePrefix = Database.nullTo(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_TABLENAME_PREFIX), 
				VersionProxy.getVersion().getInternalProductName() + "_");
		
		if (dbDatabase == null)
			throw new SQLException("Please specify a database name in the configuration.");
		
		if (dbPort != null)
			dbServer += ":" + dbPort;
	
		Database database;
		
		String url = "jdbc:postgresql://" + dbServer + "/" + dbDatabase;
		try {
			database = new Database(url, dbUser, dbPasswd, dbTablenamePrefix);
		} catch (IOException e) {
			throw new SQLException("Database connection failed. Check the configuration.");
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
