package org.reldb.wrapd.sqldb;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.reldb.TestDirectory;

public class TestPostgreSQL {

	private static final String baseDir = TestDirectory.Is + "PostgreSQL";

    private static final String prompt = "[TSET]";
    private static final String title = "TestPostgreSQL";
    private static final String tupleClassName = "TestSelectPostgreSQL";
	private static final String testClassName = "TestPostgreSQL_Source01";
	private static final String testPackage = "org.reldb.wrapd.tuples.generated";

	public static Database getDatabase(String prompt) throws SQLException, IOException {
		// settings should match PostgreSQL configuration in docker-compose.yml
		String dbHost = "localhost";
		String dbDatabase = "wrapd_testdb";
		String dbUser = "user";
		String dbPassword = "password";
		String dbTablenamePrefix = "Wrapd_";

		String url = "jdbc:postgresql://" + dbHost + "/" + dbDatabase;
		try {
			return new Database(url, dbUser, dbPassword, dbTablenamePrefix, null);
		} catch (IOException e) {
			throw new SQLException(prompt + " Database connection failed. Error is: " + e);
		}
	}

	@Test
	public void testCodeThatUsesGeneratedTuple() throws IOException, ClassNotFoundException, SQLException {
		var helper = new Helper(baseDir);
		helper.test(prompt, title, tupleClassName, testPackage, testClassName, getDatabase(prompt));
	}

}

