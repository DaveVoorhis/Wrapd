package org.reldb.wrapd.sqldb.mssql;

import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.sqldb.Pool;

import java.sql.SQLException;

public class GetDatabase {

	public static Database getDatabase() throws SQLException {
		return new Database(new Pool(
				Configuration.dbURL,
				Configuration.dbUser,
				Configuration.dbPassword
			).getDataSource(),
			Configuration.dbTablenamePrefix,
			null
		);
	}

}

