package org.reldb.wrapd.sqldb.sqlite;

import org.reldb.wrapd.TestConfiguration;

public class Configuration {

	public static final String dbName = "SQLite";
	public static final String dbPackage = "sqlite";
	public static final String dbTablenamePrefix = "Wrapd_";

	public static final String dbDatabase = "sqlitedb.sqlite";

	public static final String dbURLPrefix = "jdbc:sqlite";
	public static final String dbURL = dbURLPrefix + ":" + TestConfiguration.Directory + dbName + "/" + dbDatabase;
}

