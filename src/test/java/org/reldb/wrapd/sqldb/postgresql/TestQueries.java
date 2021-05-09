package org.reldb.wrapd.sqldb.postgresql;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.sqldb.TestHelper;
import org.reldb.wrapd.sqldb.QueryDefiner;

public class TestQueries {

	private static final String testStagePrompt = "[TSET]";

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
		new TestHelper(Configuration.dbPackage,	Configuration.dbName).test(getDatabase(testStagePrompt));
	}

}

