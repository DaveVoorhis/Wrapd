package org.reldb.wrapd.sqldb.postgresql;

import java.io.IOException;
import java.sql.SQLException;

import org.reldb.wrapd.sqldb.Database;

public class GetDatabase {

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

}

