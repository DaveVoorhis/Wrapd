package org.reldb.wrapd.sqldb;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reldb.wrapd.compiler.ForeignCompilerJava.CompilationResults;
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
		return (boolean) database.queryAll(connection, query, result -> {
			CompilationResults compilationResult = null;
			try {
				compilationResult = ResultSetToTuple.createTuple(codeDirectory, tupleClassName, result);
			} catch (ClassNotFoundException e) {
				log.error("ERROR: createTupleFromQueryAll failed in createTuple due to: " + e);
				return false;
			}
			if (!compilationResult.compiled)
				log.error("ERROR: createTupleFromQueryAll failed to compile due to: " + compilationResult.compilerMessages);
			return compilationResult.compiled;
		});
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
		var transaction = database.new Transaction(connection -> createTupleFromQueryAll(database, connection, codeDirectory, tupleClassName, query));
		var transactionResult = transaction.getResult();
		if (transactionResult.thrown == null)
			return transactionResult.success;
		log.error("ERROR: createTupleFromQueryAll failed due to: " + transactionResult.thrown);
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Tuple> Stream<T> queryAll(Database database, Connection connection, String query, Class<T> tupleClass) throws SQLException {
		return (Stream<T>)database.queryAll(connection, query, result -> {
			try {
				return ResultSetToTuple.toStream(result, tupleClass);
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | 
					IllegalArgumentException | InvocationTargetException | NoSuchFieldException e) {
				log.error("ERROR: queryAll failed due to: ", e);
			}
			return null;
		});
	}

	@SuppressWarnings("unchecked")
	public static <T extends Tuple> Stream<T> queryAll(Database database, String query, Class<T> tupleClass) throws SQLException {
		return (Stream<T>)database.queryAll(query, result -> {
			try {
				return ResultSetToTuple.toStream(result, tupleClass);
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | 
					IllegalArgumentException | InvocationTargetException | NoSuchFieldException e) {
				log.error("ERROR: queryAll failed due to: ", e);
			}
			return null;
		});
	}
	
	/*
	@SuppressWarnings("unchecked")
	public static Stream<? extends Tuple> queryAll(Database database, String query, Class<? extends Tuple> tupleClass) throws SQLException {
		TransactionReturner<Stream <?>> transaction = database.new TransactionReturner<Stream <?>>(connection -> {
			transaction.setReturnValue(queryAll(database, connection, codeDirectory, query, tupleClass));
			return true;
		});
		return (Stream<? extends Tuple>) transaction.getReturnValue();
	}
	*/
	
}
