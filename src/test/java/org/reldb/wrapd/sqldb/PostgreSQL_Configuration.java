package org.reldb.wrapd.sqldb;

import org.reldb.TestDirectory;

public class PostgreSQL_Configuration {

	public static final String baseDir = TestDirectory.Is + "PostgreSQL";
	public static final String codeDir = baseDir + "/code";

	// This test assumes use of docker-compose.yml in project root.
	//
	//   Launch from root of project via:
	//			docker-compose up -d
	//   Shut down via:
	//		    docker-compose down -v
	//
	// Settings below should match PostgreSQL configuration in docker-compose.yml

	public static final String dbHost = "localhost";
	public static final String dbDatabase = "wrapd_testdb";
	public static final String dbUser = "user";
	public static final String dbPassword = "password";
	public static final String dbTablenamePrefix = "Wrapd_";

	public static final String dbURLPrefix = "jdbc:postgresql";
	public static final String dbURL = dbURLPrefix + "://" + dbHost + "/" + dbDatabase;
}

