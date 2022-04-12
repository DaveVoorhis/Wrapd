package org.reldb.wrapd.sqldb.mssql;

public class Configuration {

	public static final String dbName = "MSSQL";
	public static final String dbPackage = "mssql";
	public static final String dbTablenamePrefix = "Wrapd_";
	public static final String dbHost = "localhost";

	// This test assumes use of docker-compose.yml in project root.
	//
	//   Launch from root of project via:
	//			docker-compose up -d
	//   Shut down via:
	//		    docker-compose down -v
	//
	// Settings below should match MySQL configuration in docker-compose.yml

	public static final String dbDatabase = "wrapd_testdb";
	public static final String dbUser = "SA";
	public static final String dbPassword = "pass@Word";

	// End settings

	public static final String dbURLPrefix = "jdbc:sqlserver";
	public static final String dbURL = dbURLPrefix + "://" + dbHost + ";encrypt=true;trustServerCertificate=true;"; // + ";databaseName=" + dbDatabase;
}
