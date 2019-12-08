package org.reldb.wrapd.sqldb;

import java.io.IOException;
import java.lang.reflect.Field;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reldb.wrapd.tuples.Tuple;
import org.reldb.wrapd.tuples.TupleTypeGenerator;

import com.mchange.v2.c3p0.DataSources;

/** Database access layer. */
public class Database {
	
	private static Logger log = LogManager.getLogger(Database.class.toString());
	
	private DataSource pool = null;	
	private String dbTablenamePrefix = "";

	/**
	 * Open a database, given a database URL, user name, password, and table name prefix.
	 * 
	 * @param dbURL - database URL
	 * @param dbUser - database user
	 * @param dbPassword - database password
	 * @param dbTablenamePrefix - table name prefix
	 * 
	 * @throws IOException
	 */
	public Database(String dbURL, String dbUser, String dbPassword, String dbTablenamePrefix) throws IOException {
		if (dbURL == null)
			throw new IllegalArgumentException("dbURL must not be null");
		
		this.dbTablenamePrefix = nullToEmptyString(dbTablenamePrefix);
		
		Properties props = new Properties();
		if (dbUser != null)
			props.setProperty("user", dbUser);
		if (dbPassword != null)
			props.setProperty("password", dbPassword);
		
		if (pool == null)
			try {
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
	
	private void showSQL(String location, String query) {
		log.debug(location + ": " + query);
	}

	/**
	 * Return type from connection use.
	 */
	public class ConnectionUseResult<T> {
		public final T returnValue;
		public final SQLException exception;
		
		public ConnectionUseResult(T t) {
			returnValue = t;
			exception = null;
		}
		
		public ConnectionUseResult(SQLException t) {
			returnValue = null;
			exception = t;
		}
	}
	
	/**
	 * Used to define lambda expressions that make use of a Connection and return a value of type T.
	 *
	 * @param <T>
	 */
	@FunctionalInterface
	public interface ConnectionUser<T> {
		public T go(Connection c) throws SQLException;
	}
	
	/**
	 * Use a connection.
	 * 
	 * @param <T> type of return value from use of connection.
	 * @param connectionUser - Instance of ConnectionUser, usually as a lambda expression.
	 * @return A ConnectionUseResult<T> containing either a T (indicating success) or a SQLException.
	 * @throws SQLException 
	 */
	public <T> ConnectionUseResult<T> processConnection(ConnectionUser<T> connectionUser) throws SQLException {
		try (Connection conn = pool.getConnection()) {
			try {
				return new ConnectionUseResult<T>(connectionUser.go(conn));
			} catch (SQLException t) {
				return new ConnectionUseResult<T>(t);
			}
		}
	}
	
	/**
	 * Use a connection.
	 * 
	 * @param <T> type of return value from user of connection.
	 * @param connectionUser - Instance of ConnectionUser, usually as a lambda expression.
	 * @return A value of type T as a result of using a Connection.
	 * @throws SQLException
	 */
	public <T> T useConnection(ConnectionUser<T> connectionUser) throws SQLException {
		var result = processConnection(connectionUser);
		if (result.exception != null)
			throw result.exception;
		return result.returnValue;
	}
	
	/**
	 * Used to define lambda expressions that receive a ResultSet for processing. T specifies the type of the return value from processing the ResultSet.
	 *
	 * @param <T>
	 */
	@FunctionalInterface
	public interface ResultSetReceiver<T> {
		public T go(ResultSet r) throws SQLException;
	}
	
	/**
	 * Issue a SELECT query, process it, and return the result
	 * 
	 * @param <T> return type
	 * @param connection - java.sql.Connection
	 * @param query - query
	 * @param receiver - result set receiver lambda
	 * @return 
	 * @return return value
	 * 
	 * @throws SQLException
	 */
	public <T> T queryAll(Connection connection, String query, ResultSetReceiver<T> receiver) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			var sqlized = replaceTableNames(query);
			showSQL("queryAll: ", sqlized);
			try (ResultSet rs = statement.executeQuery(sqlized)) {
				return receiver.go(rs);
			}
		}
	}
	
	/**
	 * Issue an update query.
	 * 
	 * @param connection - java.sql.Connection
	 * @param sqlStatement - String SQL query
	 * @return true if a ResultSet is returned, false otherwise
	 * 
	 * @throws SQLException
	 */
	public boolean updateAll(Connection connection, String sqlStatement) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			var sqlized = replaceTableNames(sqlStatement);
			showSQL("updateAll: ", sqlized);
			return statement.execute(sqlized);
		}
	}
	
	/**
	 * Issue a SELECT query and obtain a value for the first row in a specified column name. Intended to obtain a single value.
	 * 
	 * @param connection - java.sql.Connection
	 * @param query - SELECT query
	 * @param columnName - column name from which to retrieve first row's value
	 * @return - value of first row in columnName
	 * 
	 * @throws SQLException
	 */
	public Object valueOfAll(Connection connection, String query, String columnName) throws SQLException {
		return queryAll(connection, query, rs -> {
			if (rs.next())
				return rs.getObject(columnName);
			return null;
		});
	}
	
	/**
	 * Issue a SELECT query, process it, and return the result
	 * 
	 * @param <T> return type
	 * @param query - query
	 * @param receiver - result set receiver lambda
	 * @return return value
	 * 
	 * @throws SQLException
	 */
	public <T> T queryAll(String query, ResultSetReceiver<T> receiver) throws SQLException {
		return useConnection(conn -> queryAll(conn, query, receiver));
	}
	
	/**
	 * Issue an update query.
	 * 
	 * @param sqlStatement - String SQL query
	 * @return true if a ResultSet is returned, false otherwise
	 * 
	 * @throws SQLException
	 */
	public boolean updateAll(String sqlStatement) throws SQLException {
		return useConnection(conn -> updateAll(conn, sqlStatement));
	}
	
	/**
	 * Issue a SELECT query and obtain a value for the first row in a specified column name. Intended to obtain a single value.
	 * 
	 * @param query - SELECT query
	 * @param columnName - column name from which to retrieve first row's value
	 * @return - value of first row in columnName
	 * 
	 * @throws SQLException
	 */
	public Object valueOfAll(String query, String columnName) throws SQLException {
		return useConnection(conn -> valueOfAll(conn, query, columnName));
	}
	
	/**
	 * Represents a SQL NULL on behalf of a specified SQL type from the Types enum.
	 */
	public static class Null {
		private int type;
		public Null(int type) {
			this.type = type;
		}
		int getType() {return type;}
	}
	
	// Canonical setup of prepared statement parameters from Java types.
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
	
	
	/**
	 * Return type from parametric query (prepared statement) use.
	 */
	public class PreparedStatementUseResult<T> {
		public final T returnValue;
		public final SQLException exception;
		
		public PreparedStatementUseResult(T t) {
			returnValue = t;
			exception = null;
		}
		
		public PreparedStatementUseResult(SQLException t) {
			returnValue = null;
			exception = t;
		}
	}
	
	/**
	 * Used to define lambda expressions that make use of a PreparedStatement and return a value of type T.
	 *
	 * @param <T>
	 */
	@FunctionalInterface
	public interface PreparedStatementUser<T> {
		public T go(PreparedStatement ps) throws SQLException;
	}
	
	/**
	 * Use a prepared statement.
	 * 
	 * @param <T> type of return value from use of connection.
	 * @param preparedStatementUser - Instance of PreparedStatementUser, usually as a lambda expression.
	 * @return A PreparedStatementUseResult<T> containing either a T (indicating success) or a SQLException.
	 * @throws SQLException 
	 */
	public <T> PreparedStatementUseResult<T> processPreparedStatement(PreparedStatementUser<T> preparedStatementUser, Connection connection, String query, Object ... parms) throws SQLException {
		var sqlized = replaceTableNames(query);
		showSQL("processPreparedStatement: ", sqlized);
		int argCount = parms.length;
		int parmCount = (int)sqlized.chars().filter(ch -> ch == '?').count();
		if (argCount != parmCount)
			throw new IllegalArgumentException("ERROR: processPreparedStatement: Number of parameters (" + parmCount + ") doesn't match number of arguments (" + argCount + ") in " + sqlized);
		try (PreparedStatement statement = connection.prepareStatement(sqlized)) {
			setupParms(statement, parms);
			try {
				return new PreparedStatementUseResult<T>(preparedStatementUser.go(statement));
			} catch (SQLException t) {
				return new PreparedStatementUseResult<T>(t);
			}
		}
	}
	
	/**
	 * Use a prepared statement.
	 * 
	 * @param <T> type of return value from user of connection.
	 * @param preparedStatementUser - Instance of PreparedStatementUser, usually as a lambda expression.
	 * @return A value of type T as a result of using a PreparedStatement.
	 * @throws SQLException
	 */
	public <T> T usePreparedStatement(PreparedStatementUser<T> preparedStatementUser, Connection connection, String query, Object ... parms) throws SQLException {
		var result = processPreparedStatement(preparedStatementUser, connection, query, parms);
		if (result.exception != null)
			throw result.exception;
		return result.returnValue;
	}
	
	/**
	 * Issue a parametric SELECT query with '?' substitutions, process it, and return the result
	 * 
	 * @param <T> return type
	 * @param connection - java.sql.Connection
	 * @param query - query
	 * @param receiver - result set receiver lambda
	 * @param parms - parameters
	 * 
	 * @return return value
	 * 
	 * @throws SQLException
	 */
	public <T> T query(Connection connection, String query, ResultSetReceiver<T> receiver, Object ... parms) throws SQLException {
		return usePreparedStatement(statement -> {
			try (ResultSet rs = statement.executeQuery()) {
				return receiver.go(rs);
			}
		}, connection, query, parms);
	}

	/**
	 * Issue a parametric update query with '?' substitutions.
	 * 
	 * @param connection - java.sql.Connection
	 * @param query - String SQL query
	 * @param parms - parameters
	 * @return true if a ResultSet is returned, false otherwise
	 * 
	 * @throws SQLException
	 */
	public boolean update(Connection connection, String query, Object ... parms) throws SQLException {
		return usePreparedStatement(statement -> statement.execute(), connection, query, parms);
	}
	
	/**
	 * Issue a parametric SELECT query with '?' substitutions and obtain a value for the first row in a specified column name. Intended to obtain a single value.
	 * 
	 * @param connection - java.sql.Connection
	 * @param query - SELECT query
	 * @param columnName - column name from which to retrieve first row's value
	 * @param parms - parameters
	 * 
	 * @return - value of first row in columnName
	 * 
	 * @throws SQLException
	 */	
	public Object valueOf(Connection connection, String query, String columnName, Object ... parms) throws SQLException {
		return query(connection, query, rs -> {
			if (rs.next())
				return rs.getObject(columnName);
			return null;
		}, parms);
	}

	/**
	 * Issue a parametric SELECT query with '?' substitutions, process it, and return the result
	 * 
	 * @param <T> return type
	 * @param query - query
	 * @param receiver - result set receiver lambda
	 * @param parms - parameters
	 * 
	 * @return return value
	 * 
	 * @throws SQLException
	 */
	public <T> T query(String query, ResultSetReceiver<T> receiver, Object ... parms) throws SQLException {
		return useConnection(conn -> query(conn, query, receiver, parms));
	}
	
	/**
	 * Issue a parametric update query with '?' substitutions.
	 * 
	 * @param query - String SQL query
	 * @param parms - parameters
	 * @return true if a ResultSet is returned, false otherwise
	 * 
	 * @throws SQLException
	 */
	public boolean update(String query, Object ... parms) throws SQLException {
		return useConnection(conn -> update(conn, query, parms));
	}
	
	/**
	 * Issue a parametric SELECT query with '?' substitutions and obtain a value for the first row in a specified column name. Intended to obtain a single value.
	 * 
	 * @param query - SELECT query
	 * @param columnName - column name from which to retrieve first row's value
	 * @param parms - parameters
	 * 
	 * @return - value of first row in columnName
	 * 
	 * @throws SQLException
	 */	
	public Object valueOf(String query, String columnName, Object ... parms) throws SQLException {
		return useConnection(conn -> valueOf(conn, query, columnName, parms));
	}
	
	/**
	 * Insert a given Tuple.
	 * 
	 * @param connection - java.sql.Connection
	 * @param tableName - table name
	 * @param tuple - Tuple derivative.
	 * @return - should return false
	 * @throws SQLException on failure
	 */
	public boolean insert(Connection connection, String tableName, Tuple tuple) throws SQLException {
		Supplier<Stream<Field>> dataFields = () -> TupleTypeGenerator.getDataFields(tuple.getClass());
		Supplier<Stream<String>> columns = () -> dataFields.get().map(field -> field.getName());
		var columnNames = columns.get().collect(Collectors.joining(", "));
		var parms = "?"
				.repeat((int)columns.get().count())
				.replaceAll(".(?!$)", "$0, ");
		var sql = "INSERT INTO " + tableName + "(" + columnNames + ") VALUES (" + parms + ")";
		var columnValues = dataFields
			.get()
			.map(field -> {
				try {
					return field.get(tuple);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					log.error("ERROR: insert failed on field " + field.getName() + ": " + e);
					return null;
				}
			})
			.toArray(Object[]::new);
		return update(connection, sql, columnValues);
	}

	/**
	 * Insert a given Tuple.
	 * 
	 * @param tableName - table name
	 * @param tuple - Tuple derivative.
	 * @return - should return false
	 * @throws SQLException on failure
	 */
	public boolean insert(String tableName, Tuple tuple) throws SQLException {
		return useConnection(conn -> insert(conn, tableName, tuple));
	}

	/**
	 * Given multiple argument arrays, combine them into one unified argument list for passing to a parametrised query's "Object ... parms", above.
	 * 
	 * @param parms
	 * @return
	 */
	public static Object[] allArguments(Object ... parms) {
		Vector<Object> newArgs = new Vector<Object>();
		for (Object parm: parms)
			if (parm instanceof Object[])
				Collections.addAll(newArgs, (Object[])parm);
			else
				newArgs.add(parm);
		return newArgs.toArray();
	}
	
	/**
	 * FunctionalInterface to define lambdas for transactional processing.
	 */
	@FunctionalInterface
	public static interface TransactionRunner {
		public boolean run(Connection connection) throws SQLException;
	}
	
	/**
	 * Result of executing a transaction in Transaction.
	 */
	public static class TransactionResult {

		/** True if transaction committed. False if transaction rolled back. */
		public final boolean success;
		
		/** Null if transaction committed. Non-null, and contains Throwable if transaction rolled back due to exception. */ 
		public final Throwable thrown;
		
		protected TransactionResult(boolean success) {
			this.success = success;
			this.thrown = null;
		}
		
		protected TransactionResult(Throwable thrown) {
			this.success = false;
			this.thrown = thrown;
		}
	}
	
	/**
	 * Encapsulates a transaction.
	 */
	public class Transaction {
		
		private TransactionResult result = null;
		
		/**
		 * Encapsulate a transaction.
		 * 
		 * @param transactionRunner - lambda defining code to run within a transaction. If it throws an error or returns false, the transaction is rolled back.
		 * 
		 * @throws SQLException
		 */
		public Transaction(TransactionRunner transactionRunner) throws SQLException {
			try (Connection connection = pool.getConnection()) {
				connection.setAutoCommit(false);
				boolean success = false;
				try {
					success = transactionRunner.run(connection);
				} catch (Throwable t) {
					connection.rollback();
					result = new TransactionResult(t);
					return;
				}
				if (success)
					connection.commit();
				else
					connection.rollback();
				result = new TransactionResult(success);
			}
		}
		
		/**
		 * Obtain transaction execution result.
		 * 
		 * @return TransactionResult
		 */
		public TransactionResult getResult() {
			return result;
		}
	}
	
	/**
	 * If the String argument is null or an empty string, return null.
	 * 
	 * @param str - String
	 * 
	 * @return String or null
	 */
	public static String emptyToNull(String str) {
		return (str == null || str.trim().length() == 0) ? null : str;
	}
	
	/**
	 * If the String argument is null or an empty string, return a specified replacement string.
	 * 
	 * @param str - String
	 * @param replacement - String replacement
	 * 
	 * @return - String
	 */
	public static String nullTo(String str, String replacement) {
		return (emptyToNull(str) == null) ? replacement : str;
	}
	
	/**
	 * If the String argument is null or an empty string, return an empty string.
	 * 
	 * @param str - String
	 * 
	 * @return - String
	 */
	public static String nullToEmptyString(String str) {
		return nullTo(str, "");
	}
	
}
