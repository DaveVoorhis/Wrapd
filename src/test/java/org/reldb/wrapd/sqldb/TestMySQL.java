package org.reldb.wrapd.sqldb;

import org.junit.jupiter.api.Test;
import org.reldb.TestDirectory;

import java.io.IOException;
import java.sql.SQLException;

public class TestMySQL {

	private static final String baseDir = TestDirectory.Is + "MySQL";

    private static final String prompt = "[TSET]";
    private static final String title = "TestMySQL";
    private static final String tupleClassName = "TestSelectMySQL";
	private static final String testClassName = "TestMySQL_Source01";
	private static final String testPackage = "org.reldb.wrapd.tuples.generated";

	public static Database getDatabase(String prompt) throws SQLException {
		// settings should match PostgreSQL configuration in docker-compose.yml
		String dbHost = "localhost";
		String dbDatabase = "wrapd_testdb";
		String dbUser = "user";
		String dbPassword = "password";
		String dbTablenamePrefix = "Wrapd_";

		String url = "jdbc:mysql://" + dbHost + "/" + dbDatabase;
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

