package org.reldb.wrapd.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.reldb.wrapd.compiler.ForeignCompilerJava.CompilationResults;
import org.reldb.wrapd.tuples.TupleTypeGenerator;

public class ResultSetToTuple {
	
	static public CompilationResults resultSetToTuple(String codeDir, String tupleName, ResultSet results) throws SQLException, ClassNotFoundException {
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
	
}
