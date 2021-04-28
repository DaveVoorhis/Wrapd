package org.reldb.wrapd.sqldb;

import org.junit.jupiter.api.Test;
import org.reldb.TestDirectory;

import java.io.IOException;
import java.sql.SQLException;

public class MySQL_Configuration {

	public static final String baseDir = TestDirectory.Is + "MySQL";

	// This test assumes use of docker-compose.yml in project root.
	//
	//   Launch from root of project via:
	//			docker-compose up -d
	//   Shut down via:
	//		    docker-compose down -v
	//
	// Settings below should match MySQL configuration in docker-compose.yml

	public static final String dbHost = "localhost";
	public static final String dbDatabase = "wrapd_testdb";
	public static final String dbUser = "user";
	public static final String dbPassword = "password";
	public static final String dbTablenamePrefix = "Wrapd_";

	public static final String dbURLPrefix = "jdbc:mysql";
	public static final String dbURL = dbURLPrefix + "://" + dbHost + "/" + dbDatabase;

}

