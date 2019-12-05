package org.reldb.wrapd.sqldb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reldb.wrapd.compiler.ForeignCompilerJava.CompilationResults;
import org.reldb.wrapd.sqldb.Database.ResultSetReceiver;
import org.reldb.wrapd.sqldb.Database.Transaction;
import org.reldb.wrapd.tuples.Tuple;

/**
 * Tools for making queries assuming a Tuple class exists to represent rows.
 * 
 * @author dave
 *
 */
public class Query {

	private static Logger log = LogManager.getLogger(Query.class.toString());
		
	/**
	 * Obtain a lambda to generate a new Tuple-derived class from a ResultSet.
	 * 
	 * @param codeDirectory - directory into which generated class (both source and .class) will be placed.
	 * @param tupleClassName - name for new tuple class
	 * @return - lambda which will generate the class given a ResultSet.
	 */
	public static ResultSetReceiver<Boolean> newResultSetGeneratesTupleClass(String codeDirectory, String tupleClassName) {
		return resultSet -> {
			CompilationResults compilationResult = null;
			try {
				compilationResult = ResultSetToTuple.createTuple(codeDirectory, tupleClassName, resultSet);
			} catch (ClassNotFoundException e) {
				log.error("ERROR: tuple generator failed in createTuple due to: " + e);
				return false;
			}
			if (!compilationResult.compiled)
				log.error("ERROR: tuple class failed to compile due to: " + compilationResult.compilerMessages);
			return compilationResult.compiled;
		};
	}
	
	private static boolean processGenerateTupleResultTransaction(Transaction transaction) {
		var transactionResult = transaction.getResult();
		if (transactionResult.thrown == null)
			return transactionResult.success;
		log.error("ERROR: Generating tuple class from query failed due to: " + transactionResult.thrown);
		return false;
	}
	
	/**
	 * Use a SELECT query to generate a corresponding Tuple-derived class to represent future evaluations of the same query or similar queries.
	 * 
	 * @param database - Database
	 * @param connection - java.sql.Connection, usually obtained via a Transaction
	 * @param codeDirectory - directory in which compiled Tuple-derived source and .class will be generated
	 * @param tupleClassName - desired Tuple-derived class name
	 * @param query - String - query to be evaluated
	 * @return - true if Tuple-derived class has been created and compiled; false otherwise
	 * 
	 * @throws SQLException
	 */
	public static boolean createTupleFromQueryAll(Database database, Connection connection, String codeDirectory, String tupleClassName, String query) throws SQLException {
		return database.queryAll(connection, query, newResultSetGeneratesTupleClass(codeDirectory, tupleClassName));
	}
	
	/**
	 * Use a SELECT query to generate a corresponding Tuple-derived class to represent future evaluations of the same query or similar queries.
	 * 
	 * @param database - Database
	 * @param codeDirectory - directory in which compiled Tuple-derived source and .class will be generated
	 * @param tupleClassName - desired Tuple-derived class name
	 * @param query - String - query to be evaluated
	 * @return - true if Tuple-derived class has been created and compiled; false otherwise
	 * 
	 * @throws SQLException
	 */
	public static boolean createTupleFromQueryAll(Database database, String codeDirectory, String tupleClassName, String query) throws SQLException {
		return processGenerateTupleResultTransaction(database.new Transaction(connection -> createTupleFromQueryAll(database, connection, codeDirectory, tupleClassName, query)));
	}

	/**
	 * Use a parametric SELECT query to generate a corresponding Tuple-derived class to represent future evaluations of the same query or similar queries.
	 * 
	 * @param database - Database
	 * @param connection - java.sql.Connection, usually obtained via a Transaction
	 * @param codeDirectory - directory in which compiled Tuple-derived source and .class will be generated
	 * @param tupleClassName - desired Tuple-derived class name
	 * @param query - String - query to be evaluated
	 * @param parms - parameters which positionally match to '?' in the query
	 * @return - true if Tuple-derived class has been created and compiled; false otherwise
	 * 
	 * @throws SQLException
	 */
	public static boolean createTupleFromQuery(Database database, Connection connection, String codeDirectory, String tupleClassName, String query, Object ... parms) throws SQLException {
		return database.query(connection, query, newResultSetGeneratesTupleClass(codeDirectory, tupleClassName), parms);
	}
	
	/**
	 * Use a SELECT query to generate a corresponding Tuple-derived class to represent future evaluations of the same query or similar queries.
	 * 
	 * @param database - Database
	 * @param codeDirectory - directory in which compiled Tuple-derived source and .class will be generated
	 * @param tupleClassName - desired Tuple-derived class name
	 * @param query - String - query to be evaluated
	 * @param parms - parameters which positionally match to '?' in the query
	 * @return - true if Tuple-derived class has been created and compiled; false otherwise
	 * 
	 * @throws SQLException
	 */
	public static boolean createTupleFromQuery(Database database, String codeDirectory, String tupleClassName, String query, Object ... parms) throws SQLException {
		return processGenerateTupleResultTransaction(database.new Transaction(connection -> createTupleFromQuery(database, connection, codeDirectory, tupleClassName, query, parms)));
	}

	/**
	 * Obtain a lambda that converts a ResultSet to a Stream<T> where T extends Tuple.
	 * 
	 * @param <T> - T extends Tuple
	 * @param tupleClass - the stream will be of instances of tupleClass
	 * @return Stream<T>
	 */
	public static <T extends Tuple> ResultSetReceiver<Stream<T>> newResultSetToStream(Class<T> tupleClass) {
		return result -> {
			try {
				return ResultSetToTuple.toStream(result, tupleClass);
			} catch (Throwable e) {
				log.error("ERROR: ResultSet to Stream conversion failed due to: ", e);
				return null;
			}
		};
	}
	
	/** 
	 * Obtain a stream of Tuple derivatives from a query evaluation.
	 * 
	 * @param <T> - T extends Tuple.
	 * @param database - Database
	 * @param connection - a java.sql.Connection, typically obtained via a Transaction
	 * @param query - query string
	 * @param tupleClass - Tuple derivative that represents rows in the ResultSet returned from evaluating the query
	 * @return Stream<T>
	 * @throws SQLException
	 */
	public static <T extends Tuple> Stream<T> queryAll(Database database, Connection connection, String query, Class<T> tupleClass) throws SQLException {
		return database.queryAll(connection, query, newResultSetToStream(tupleClass));
	}

	/** 
	 * Obtain a stream of Tuple derivatives from a query evaluation.
	 * 
	 * @param <T> - T extends Tuple.
	 * @param database - Database
	 * @param query - query string
	 * @param tupleClass - Tuple derivative that represents rows in the ResultSet returned from evaluating the query
	 * @return Stream<T>
	 * @throws SQLException
	 */
	public static <T extends Tuple> Stream<T> queryAll(Database database, String query, Class<T> tupleClass) throws SQLException {
		return database.queryAll(query, newResultSetToStream(tupleClass));
	}
	
	/** 
	 * Obtain a stream of Tuple derivatives from a query evaluation.
	 * 
	 * @param <T> - T extends Tuple.
	 * @param database - Database
	 * @param connection - a java.sql.Connection, typically obtained via a Transaction
	 * @param query - query string
	 * @param tupleClass - Tuple derivative that represents rows in the ResultSet returned from evaluating the query
	 * @return Stream<T>
	 * @throws SQLException
	 */
	public static <T extends Tuple> Stream<T> query(Database database, Connection connection, String query, Class<T> tupleClass, Object ... parms) throws SQLException {
		return database.query(connection, query, newResultSetToStream(tupleClass), parms);
	}

	/** 
	 * Obtain a stream of Tuple derivatives from a query evaluation.
	 * 
	 * @param <T> - T extends Tuple.
	 * @param database - Database
	 * @param query - query string
	 * @param tupleClass - Tuple derivative that represents rows in the ResultSet returned from evaluating the query
	 * @return Stream<T>
	 * @throws SQLException
	 */
	public static <T extends Tuple> Stream<T> query(Database database, String query, Class<T> tupleClass, Object ... parms) throws SQLException {
		return database.query(query, newResultSetToStream(tupleClass), parms);
	}
	
}
