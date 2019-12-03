package org.reldb.wrapd.sqldb;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reldb.wrapd.compiler.ForeignCompilerJava.CompilationResults;

/**
 * Tools for making queries assuming a Tuple class exists to represent rows.
 * 
 * @author dave
 *
 */
public class Query {

	private static Logger log = LogManager.getLogger(Query.class.toString());
		
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
	
	public static boolean createTupleFromQueryAll(Database database, String codeDirectory, String tupleClassName, String query) throws SQLException {
		var transaction = database.new Transaction(connection -> createTupleFromQueryAll(database, connection, codeDirectory, tupleClassName, query));
		var transactionResult = transaction.getResult();
		if (transactionResult.thrown == null)
			return transactionResult.success;
		log.error("ERROR: createTupleFromQueryAll failed due to: " + transactionResult.thrown);
		return false;
	}
	

	
}
