package org.reldb.wrapd.db;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;
import java.util.LinkedList;
import java.util.List;

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
	 * 
	 * @return - CompilationResults.
	 * 
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
	 * 
	 * @return Stream of specified Tuple subclass instances.
	 * 
	 * @throws SecurityException - thrown if tuple constructor is not accessible
	 * @throws NoSuchMethodException - thrown if tuple constructor doesn't exist
	 * @throws InvocationTargetException - thrown if unable to instantiate tuple class
	 * @throws IllegalArgumentException  - thrown if unable to instantiate tuple class, or if there is a type mismatch assigning tuple field values
	 * @throws IllegalAccessException  - thrown if unable to instantiate tuple class
	 * @throws InstantiationException - thrown if unable to instantiate tuple class
	 * @throws SQLException - thrown if accessing ResultSet fails
	 * @throws NoSuchFieldException - thrown if a given ResultSet field name cannot be found in the Tuple
	 */
	public static Stream<? extends Tuple> toStream(ResultSet resultSet, Class<? extends Tuple> tupleType) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException, NoSuchFieldException {
		var tupleConstructor = tupleType.getConstructor((Class<?>[])null);
		var metadata = resultSet.getMetaData();
		List<Tuple> rows = new LinkedList<>();
		// TODO - make faster by using metadata to store 'field' references in an array indexed by column
		while (resultSet.next()) {
			var tuple = tupleConstructor.newInstance((Object[])null);
			for (int column = 1; column <= metadata.getColumnCount(); column++) {
				var name = metadata.getColumnName(column);
				var value = resultSet.getObject(column);
				var field = tuple.getClass().getField(name);
				field.set(tuple, value);
			}
			rows.add(tuple);
		}
		return rows.stream();
	}
	
}
