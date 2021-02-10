package org.reldb.wrapd.sqldb;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import org.reldb.TestDirectory;
import org.reldb.toolbox.configuration.Configuration;
import org.reldb.wrapd.sqldb.sqlite.SQLiteCustomisations;

public class TestSQLite {

	private static final String baseDir = TestDirectory.Is + "SQLite";

	private static final String prompt = "[TSET]";
	private static final String title = "TestSQLite";
	private static final String tupleClassName = "TestSelectSQLite";
	private static final String testClassName = "TestSQLite_Source01";
	private static final String testPackage = "org.reldb.wrapd.tuples.generated";

	private static final String dbDatabase = "sqlitedb.sqlite";
	private static final String dbTablenamePrefix = "Wrapd_";

	private static final String dbURLPrefix = "jdbc:sqlite";
	private static final String dbURL = dbURLPrefix + ":" + Configuration.getLocation() + dbDatabase;

	private static Helper helper;

	public static Database getDatabase(String prompt) throws SQLException, IOException {
		Configuration.setLocation(baseDir);
		try {
			return new Database(dbURL, dbTablenamePrefix, new SQLiteCustomisations());
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

