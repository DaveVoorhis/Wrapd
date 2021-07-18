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

	@BeforeAll
	public static void clearDb() throws SQLException {
		Database db = getDatabase(testStagePrompt);
		System.out.println(testStagePrompt + " Clearing database " + db.getClass().getName());
		String[] tableNames = {
				"$$__version",
				"$$tester",
				"$$version"};
		for (String tableName: tableNames)
			try {
				System.out.println(testStagePrompt + " Dropping table " + tableName);
				db.updateAll("DROP TABLE " + tableName);
			} catch (SQLException sqe) {
				System.out.println(testStagePrompt + " Oops dropping table: " + sqe);
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

