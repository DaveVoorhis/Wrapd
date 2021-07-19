package org.reldb.wrapd.sqldb.mysql;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.sqldb.TestQueriesHelper;
import org.reldb.wrapd.sqldb.QueryDefiner;
import org.reldb.wrapd.sqldb.TestSchemaHelper;

public class Tests {

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
		new TestQueriesHelper(Configuration.dbPackage, Configuration.dbName).test(getDatabase(testStagePrompt));
	}

	@Test
	public void testSchema01() throws SQLException {
		new TestSchemaHelper().test01(getDatabase(testStagePrompt));
	}

}

