package org.reldb.wrapd.sqldb.sqlite;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.sqldb.Helper;
import org.reldb.wrapd.sqldb.QueryDefiner;

public class TestQueries {

	private static final String prompt = "[TSET]";
	private static final String tupleClassName = "TestSelectSQLite";
	private static final String testClassName = "TestSQLite_Source01";
	private static final String testPackage = "org.reldb.wrapd.tuples.generated";

	public static Database getDatabase(String prompt) throws SQLException {
		try {
			return new Database(Configuration.dbURL, Configuration.dbTablenamePrefix, new SQLiteCustomisations());
		} catch (IOException e) {
			throw new SQLException(prompt + " Database connection failed. Error is: " + e);
		}
	}

	@Test
	public void testCodeThatUsesGeneratedTuple() throws IOException, ClassNotFoundException, SQLException, QueryDefiner.QueryDefinerException {
		new Helper(
				Configuration.baseDir,
				Configuration.codeDir,
				prompt,
				tupleClassName,
				testPackage,
				testClassName
		).test(getDatabase(prompt));
	}

}

