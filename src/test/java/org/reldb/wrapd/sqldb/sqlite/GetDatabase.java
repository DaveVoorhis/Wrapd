package org.reldb.wrapd.sqldb.sqlite;

import java.sql.SQLException;

import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.sqldb.Pool;

public class GetDatabase {

	public static Database getDatabase() throws SQLException {
		return new Database(new Pool(
				Configuration.dbURL,
				null,
				null
			).getDataSource(),
			Configuration.dbTablenamePrefix,
			new SQLiteCustomisations()
		);
	}

}

