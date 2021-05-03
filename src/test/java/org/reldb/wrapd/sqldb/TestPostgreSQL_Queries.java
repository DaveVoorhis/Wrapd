package org.reldb.wrapd.sqldb;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

public class TestPostgreSQL_Queries {

	private static final String prompt = "[TSET]";
	private static final String tupleClassName = "TestSelectPostgreSQL";
	private static final String testClassName = "TestPostgreSQL_Source01";
	private static final String testPackage = "org.reldb.wrapd.tuples.generated";

	public static Database getDatabase(String prompt) throws SQLException {
		try {
			return new Database(
					PostgreSQL_Configuration.dbURL,
					PostgreSQL_Configuration.dbUser,
					PostgreSQL_Configuration.dbPassword,
					PostgreSQL_Configuration.dbTablenamePrefix,
					null
			);
		} catch (IOException e) {
			throw new SQLException(prompt + " Database connection failed. Error is: " + e);
		}
	}

	@Test
	public void testCodeThatUsesGeneratedTuple() throws IOException, ClassNotFoundException, SQLException {
		new Helper(
				PostgreSQL_Configuration.baseDir,
				PostgreSQL_Configuration.codeDir,
				prompt,
				tupleClassName,
				testPackage,
				testClassName
		).test(getDatabase(prompt));
	}

}

