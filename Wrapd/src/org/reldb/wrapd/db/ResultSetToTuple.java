package org.reldb.wrapd.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

import org.reldb.wrapd.compiler.ForeignCompilerJava.CompilationResults;
import org.reldb.wrapd.tuples.Tuple;
import org.reldb.wrapd.tuples.TupleTypeGenerator;

public class ResultSetToTuple {
	
	/**
	 * Given a target code directory and a desired Tuple class name, and a ResultSet, generate a Tuple class
	 * to host the ResultSet.
	 *  
	 * @param codeDir - Directory where source code will be stored.
	 * @param tupleName - Name of new Tuple class.
	 * @param results - ResultSet to be used to create the new Tuple class.
	 * @return - CompilationResults.
	 * @throws SQLException - thrown if there is a problem retrieving ResultSet metadata.
	 * @throws ClassNotFoundException - thrown if a column class specified in the ResultSet metadata can't be loaded.
	 */
	public static CompilationResults createTuple(String codeDir, String tupleName, ResultSet results) throws SQLException, ClassNotFoundException {
		var generator = new TupleTypeGenerator(codeDir, tupleName);
		var metadata = results.getMetaData();
		for (int column = 1; column <= metadata.getColumnCount(); column++) {
			var name = metadata.getColumnName(column);
			var columnClassName = metadata.getColumnClassName(column);
			var type = Class.forName(columnClassName);
			generator.addAttribute(name, type);
		}
		return generator.compile();
	}
	
	/**
	 * Convert a ResultSet to a Stream of TupleS.
	 * 
	 * @param resultSet - source ResultSet
	 * @param tupleType - subclass of Tuple. Each row will be converted to a new instance of this class.
	 * @return Stream of specified Tuple subclass instances.
	 */
	public static Stream<? extends Tuple> toStream(ResultSet resultSet, Class<? extends Tuple> tupleType) {
		
	}
	
}
