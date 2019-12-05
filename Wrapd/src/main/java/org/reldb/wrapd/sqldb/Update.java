package org.reldb.wrapd.sqldb;

import java.sql.Connection;

import org.reldb.wrapd.tuples.Tuple;

public class Update {

	public static boolean insert(Database database, Connection connection, String tableName, Tuple tupleClass) {
		String sql = "INSERT INTO " + tableName + " VALUES (";
		
		return true;
	}
		
	public static boolean insert(Database database, String tableName, Tuple tupleClass) {
		
		return true;
	}
}
