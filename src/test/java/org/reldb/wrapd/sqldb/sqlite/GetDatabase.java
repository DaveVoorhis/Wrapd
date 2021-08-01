package org.reldb.wrapd.sqldb.sqlite;

import java.io.IOException;
import java.sql.SQLException;

import org.reldb.wrapd.sqldb.Database;

public class GetDatabase {

	public static Database getDatabase() throws SQLException {
		return new Database(
			Configuration.dbURL,
			Configuration.dbTablenamePrefix,
			new SQLiteCustomisations()
		);
	}

}

