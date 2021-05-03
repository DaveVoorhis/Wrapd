package org.reldb.wrapd.sqldb;

import org.reldb.TestDirectory;

public class SQLite_Configuration {

	public static final String baseDir = TestDirectory.Is + "SQLite";
	public static final String codeDir = baseDir + "/code";

	public static final String dbDatabase = "sqlitedb.sqlite";
	public static final String dbTablenamePrefix = "Wrapd_";

	public static final String dbURLPrefix = "jdbc:sqlite";
	public static final String dbURL = dbURLPrefix + ":" + baseDir + "/" + dbDatabase;
}
