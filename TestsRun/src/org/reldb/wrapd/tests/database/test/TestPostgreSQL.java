package org.reldb.wrapd.tests.database.test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.reldb.toolbox.configuration.Configuration;
import org.reldb.wrapd.db.Database;
import org.reldb.wrapd.db.ResultSetToTuple;
import org.reldb.wrapd.db.postgresql.WrapDBConfiguration;
import org.reldb.wrapd.tuples.generated.TestSelect;
import org.reldb.wrapd.version.VersionProxy;

public class TestPostgreSQL {

	private static String baseDir = "../_TestData";
		
	@Test
	public void testQueryToStream01() throws SQLException {
		
		Configuration.setLocation(baseDir);
		
		String dbServer = Database.nullTo(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_SERVER), "localhost");
		String dbDatabase = Database.emptyToNull(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_NAME));
		String dbUser = Database.emptyToNull(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_USER));
		String dbPasswd = Database.emptyToNull(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_PASSWORD));
		String dbPort = Database.emptyToNull(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_NONSTANDARD_PORT));
		String dbTablenamePrefix = Database.nullTo(Configuration.getValue(WrapDBConfiguration.class.getName(), WrapDBConfiguration.DATABASE_TABLENAME_PREFIX), 
				VersionProxy.getVersion().getInternalProductName() + "_");
		
		if (dbDatabase == null) {
			System.out.println("Please specify a database name in the configuration.");
			return;
		}
		
		if (dbPort != null)
			dbServer += ":" + dbPort;
	
		Database database;
		
		String url = "jdbc:postgresql://" + dbServer + "/" + dbDatabase;
		try {
			database = new Database(url, dbUser, dbPasswd, dbTablenamePrefix);
		} catch (IOException e) {
			System.out.println("Database connection failed. Check the configuration.");
			return;
		}
		
		database.new Transaction(connection -> {
			database.queryAll(connection, "SELECT * FROM $$tester", result -> {
				try {
					var testSelectStream = ResultSetToTuple.toStream(result, TestSelect.class);
					testSelectStream.forEach(tuple -> System.out.println(tuple.toString()));
				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException | NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			});
			return true;
		});
		
	}
	
}
