package org.reldb.wrapd.sqldb.postgresql;

import java.io.IOException;
import java.sql.SQLException;

import org.reldb.wrapd.sqldb.Database;

public class GetDatabase {

	public static Database getDatabase() throws SQLException {
		return new Database(
				Configuration.dbURL,
				Configuration.dbUser,
				Configuration.dbPassword,
				Configuration.dbTablenamePrefix,
				null
		);
	}

}

