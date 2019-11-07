package org.reldb.wrapd.db;

import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collections;
import java.util.Properties;
import java.util.Vector;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.DataSources;

/** Database access layer. */
public class Database {
	
	private DataSource pool = null;	
	private String dbTablenamePrefix = "";

	public Database(String dbURL, String dbUser, String dbPassword, String dbTablenamePrefix) throws IOException {
		this.dbTablenamePrefix = dbTablenamePrefix;
		
		Properties props = new Properties();
		if (dbUser != null)
			props.setProperty("user", dbUser);
		if (dbPassword != null)
			props.setProperty("password", dbPassword);
		
		if (pool == null)
			try {
				System.out.println("Database: create connection pool for " + dbURL);
				DriverManager.getConnection(dbURL, props).close();
				DataSource unpooledSource = DataSources.unpooledDataSource(dbURL, props);
				pool = DataSources.pooledDataSource(unpooledSource);
			} catch (SQLException e) {
				throw new IOException("Database connection to " + dbURL + " failed. Please check that the database exists and the credentials are correct: " + e.getMessage());
			}
	}
	
	// Wherever $$ appears, replace it with dbTableNamePrefix 
	private String replaceTableNames(String query) {
		return query.replaceAll("\\$\\$", dbTablenamePrefix);
	}
	
	@FunctionalInterface
	public interface ResultSetReceiver<T> {
		public T go(ResultSet r) throws SQLException;
	}
	
	public <T> Object queryAll(Connection connection, String query, ResultSetReceiver<T> receiver) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			try (ResultSet rs = statement.executeQuery(replaceTableNames(query))) {
				return receiver.go(rs);
			}
		}
	}
	
	public void updateAll(Connection connection, String sqlStatement) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute(replaceTableNames(sqlStatement));
		}
	}
	
	public Object valueOfAll(Connection connection, String query, String columnName) throws SQLException {
		return queryAll(connection, query, rs -> {
			if (rs.next())
				return rs.getObject(columnName);
			return null;
		});
	}
	
	public <T> Object queryAll(String query, ResultSetReceiver<T> receiver) throws SQLException {
		try (Connection conn = pool.getConnection()) {
			return queryAll(conn, query, receiver);
		}
	}
	
	public void updateAll(String sqlStatement) throws SQLException {
		try (Connection conn = pool.getConnection()) {
			updateAll(conn, sqlStatement);
		}
	}
	
	public Object valueOfAll(String query, String columnName) throws SQLException {
		try (Connection conn = pool.getConnection()) {
			return valueOfAll(conn, query, columnName);
		}
	}
	
	public static class Null {
		private int type;
		public Null(int type) {
			this.type = type;
		}
		int getType() {return type;}
	}
	
	private static void setupParms(PreparedStatement statement, Object ...parms) throws SQLException {
		int parmNumber = 1;
		for (Object parm: parms) {
			if (parm == null)
				statement.setNull(parmNumber, Types.VARCHAR);
			else if (parm instanceof Null)
				statement.setNull(parmNumber, ((Null)parm).getType());
			else if (parm instanceof Integer)
				statement.setInt(parmNumber, (Integer)parm);
			else if (parm instanceof Double)
				statement.setDouble(parmNumber, (Double)parm);
			else if (parm instanceof Float)
				statement.setFloat(parmNumber, (Float)parm);
			else if (parm instanceof Date)
				statement.setDate(parmNumber, (Date)parm);
			else if (parm instanceof Long)
				statement.setLong(parmNumber, (Long)parm);
			else if (parm instanceof Blob)
				statement.setBlob(parmNumber, (Blob)parm);
			else if (parm instanceof Boolean)
				statement.setBoolean(parmNumber, (Boolean)parm);
			else
				statement.setString(parmNumber, parm.toString());
			parmNumber++;
		}		
	}
	
	public <T> Object query(Connection connection, String query, ResultSetReceiver<T> receiver, Object ... parms) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(replaceTableNames(query))) {
			setupParms(statement, parms);
			try (ResultSet rs = statement.executeQuery()) {
				return receiver.go(rs);
			}
		}
	}

	public void update(Connection connection, String query, Object ... parms) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(replaceTableNames(query))) {
			setupParms(statement, parms);
			statement.execute();
		}
	}
	
	public Object valueOf(Connection connection, String query, String columnName, Object ... parms) throws SQLException {
		return query(connection, query, rs -> {
			if (rs.next())
				return rs.getObject(columnName);
			return null;
		}, parms);
	}
	
	public <T> Object query(String query, ResultSetReceiver<T> receiver, Object ... parms) throws SQLException {
		try (Connection conn = pool.getConnection()) {
			return query(conn, query, receiver, parms);
		}
	}

	public void update(String query, Object ... parms) throws SQLException {
		try (Connection conn = pool.getConnection()) {
			update(conn, query, parms);
		}
	}
	
	public Object valueOf(String query, String columnName, Object ... parms) throws SQLException {
		try (Connection conn = pool.getConnection()) {
			return valueOf(conn, query, columnName, parms);
		}
	}

	/** Given multiple argument arrays, combine them into one unified argument list for passing to a parametrised query's "Object ... parms", above. */
	public static Object[] allArguments(Object ... parms) {
		Vector<Object> newArgs = new Vector<Object>();
		for (Object parm: parms)
			if (parm instanceof Object[])
				Collections.addAll(newArgs, (Object[])parm);
			else
				newArgs.add(parm);
		return newArgs.toArray();
	}
	
	@FunctionalInterface
	public static interface TransactionRunner {
		public boolean run(Connection connection) throws SQLException;
	}
	
	public class Transaction {
		public Transaction(TransactionRunner transactionRunner) throws SQLException {
			try (Connection connection = pool.getConnection()) {
				connection.setAutoCommit(false);
				boolean success = false;
				try {
					success = transactionRunner.run(connection);
				} catch (SQLException | Error se) {
					connection.rollback();
					throw se;
				}
				if (success)
					connection.commit();
				else
					connection.rollback();
			}
		}
	}

	public static String emptyToNull(String str) {
		if (str == null || str.trim().length() == 0)
			return null;
		return str;
	}
	
	public static String nullTo(String str, String replacement) {
		return (emptyToNull(str) == null) ? replacement : str;
	}
	
	public static String nullToEmptyString(String str) {
		return nullTo(str, "");
	}
	
}
