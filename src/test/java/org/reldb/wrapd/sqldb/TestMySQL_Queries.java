package org.reldb.wrapd.sqldb;

import org.junit.jupiter.api.Test;
import org.reldb.TestDirectory;

import java.io.IOException;
import java.sql.SQLException;

public class TestMySQL_Queries {

    private static final String prompt = "[TSET]";
    private static final String tupleClassName = "TestSelectMySQL";
	private static final String testClassName = "TestMySQL_Source01";
	private static final String testPackage = "org.reldb.wrapd.tuples.generated";

	public static Database getDatabase(String prompt) throws SQLException {
		try {
			return new Database(
					MySQL_Configuration.dbURL,
					MySQL_Configuration.dbUser,
					MySQL_Configuration.dbPassword,
					MySQL_Configuration.dbTablenamePrefix,
					null);
		} catch (IOException e) {
			throw new SQLException(prompt + " Database connection failed. Error is: " + e);
		}
	}

	@Test
	public void testCodeThatUsesGeneratedTuple() throws IOException, ClassNotFoundException, SQLException {
		new Helper(MySQL_Configuration.baseDir, prompt, tupleClassName, testPackage, testClassName).test(getDatabase(prompt));
	}

}

