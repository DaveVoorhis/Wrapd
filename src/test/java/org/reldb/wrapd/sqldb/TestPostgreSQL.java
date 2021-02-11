package org.reldb.wrapd.sqldb;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.reldb.TestDirectory;

public class TestPostgreSQL {

	private static final String baseDir = TestDirectory.Is + "PostgreSQL";

    private static final String prompt = "[TSET]";
    private static final String tupleClassName = "TestSelectPostgreSQL";
	private static final String testClassName = "TestPostgreSQL_Source01";
	private static final String testPackage = "org.reldb.wrapd.tuples.generated";

	// This test assumes use of docker-compose.yml in project root.
	//
	//   Launch from root of project via:
	//			docker-compose up -d
	//   Shut down via:
	//		    docker-compose down -v
	//
	// Settings below should match PostgreSQL configuration in docker-compose.yml

	private static final String dbHost = "localhost";
	private static final String dbDatabase = "wrapd_testdb";
	private static final String dbUser = "user";
	private static final String dbPassword = "password";
	private static final String dbTablenamePrefix = "Wrapd_";

	private static final String dbURLPrefix = "jdbc:postgresql";
	private static final String dbURL = dbURLPrefix + "://" + dbHost + "/" + dbDatabase;

	public static Database getDatabase(String prompt) throws SQLException {
		try {
			return new Database(dbURL, dbUser, dbPassword, dbTablenamePrefix, null);
		} catch (IOException e) {
			throw new SQLException(prompt + " Database connection failed. Error is: " + e);
		}
	}

	@Test
	public void testCodeThatUsesGeneratedTuple() throws IOException, ClassNotFoundException, SQLException {
		new Helper(baseDir, prompt, tupleClassName, testPackage, testClassName).test(getDatabase(prompt));
	}

}

