package org.reldb.wrapd.sqldb.sqlite;

import java.io.IOException;
import java.sql.SQLException;

import org.reldb.wrapd.sqldb.Database;

public class GetDatabase {

	public static Database getDatabase(String prompt) throws SQLException {
		try {
			return new Database(
				Configuration.dbURL,
				Configuration.dbTablenamePrefix,
				new SQLiteCustomisations()
			);
		} catch (IOException e) {
			throw new SQLException(prompt + " Database connection failed. Error is: " + e);
		}
	}

}

