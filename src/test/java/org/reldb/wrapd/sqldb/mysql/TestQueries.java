package org.reldb.wrapd.sqldb.mysql;

import org.junit.jupiter.api.Test;
import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.sqldb.Helper;
import org.reldb.wrapd.sqldb.QueryDefiner;

import java.io.IOException;
import java.sql.SQLException;

public class TestQueries {

    private static final String prompt = "[TSET]";
    private static final String tupleName = "TestMySQL";
	private static final String testSourceName = "TestMySQL_Source01";
	private static final String testPackage = "org.reldb.wrapd.tuples.generated";

	public static Database getDatabase(String prompt) throws SQLException {
		try {
			return new Database(
					Configuration.dbURL,
					Configuration.dbUser,
					Configuration.dbPassword,
					Configuration.dbTablenamePrefix,
					null
			);
		} catch (IOException e) {
			throw new SQLException(prompt + " Database connection failed. Error is: " + e);
		}
	}

	@Test
	public void testCodeThatUsesGeneratedTuple() throws IOException, ClassNotFoundException, SQLException, QueryDefiner.QueryDefinerException {
		new Helper(
				Configuration.baseDir,
				prompt,
				tupleName,
				testPackage,
				testSourceName
		).test(getDatabase(prompt));
	}

}

