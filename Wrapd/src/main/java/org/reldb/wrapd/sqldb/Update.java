package org.reldb.wrapd.sqldb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reldb.wrapd.tuples.Tuple;

public class Update {
	
	private static Logger log = LogManager.getLogger(Update.class.toString());
	
	public static void insert(Database database, Connection connection, String tableName, Tuple tuple) throws SQLException {
		var fields = tuple.getClass().getFields();
		var columnNames = Arrays.stream(fields)
			.map(field -> field.getName())
			.collect(Collectors.joining(", "));
			
		String sql = "INSERT INTO " + tableName + "(" + columnNames + ") VALUES (" + String.join(", ", "?".repeat(fields.length)) + ")";

		var columnValues = Arrays.stream(fields)
			.map(field -> {
				try {
					return field.get(tuple);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					log.error("ERROR: insert failed on field " + field.getName() + ": " + e);
					return null;
				}
			})
			.toArray(Object[]::new);

		database.update(connection, sql, columnValues);
	}
		
	public static boolean insert(Database database, String tableName, Tuple tuple) {
		try (Connection conn = pool.getConnection()) {
			return valueOfAll(conn, query, columnName);
		}
		
		return true;
	}
}
