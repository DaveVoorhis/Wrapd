package org.reldb.wrapd.sqldb.postgresql;

public class Configuration {

	public static final String dbName = "PostgreSQL";
	public static final String dbPackage = "postgresql";
	public static final String dbTablenamePrefix = "Wrapd_";
	public static final String dbHost = "localhost";

	// This test assumes use of docker-compose.yml in project root.
	//
	//   Launch from root of project via:
	//			docker-compose up -d
	//   Shut down via:
	//		    docker-compose down -v
	//
	// Settings below should match PostgreSQL configuration in docker-compose.yml

	public static final String dbDatabase = "wrapd_testdb";
	public static final String dbUser = "user";
	public static final String dbPassword = "password";

	// End settings

	public static final String dbURLPrefix = "jdbc:postgresql";
	public static final String dbURL = dbURLPrefix + "://" + dbHost + "/" + dbDatabase;
}

