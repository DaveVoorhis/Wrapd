package org.reldb.wrapd.database;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reldb.wrapd.compiler.ForeignCompilerJava;
import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.sqldb.ResultSetToTuple;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSQLite {

	private static Database database;
	private static final String prompt = "[TSET]";

	@BeforeAll
	public static void setup() throws SQLException, IOException {
		DatabaseConfigurationAndSetup.ensureTestDirectoryExists();
		System.out.println(prompt + " Executing TestSQLite setup.");
		database = DatabaseConfigurationAndSetup.getSQLiteDatabase(prompt);
		DatabaseConfigurationAndSetup.databaseTeardown(prompt, database);
		DatabaseConfigurationAndSetup.databaseCreate(prompt, database);
		final var tupleClassName = "TestSelect";
		ResultSetToTuple.destroyTuple(DatabaseConfigurationAndSetup.getCodeDirectory(), tupleClassName);
		database.createTupleFromQueryAll(DatabaseConfigurationAndSetup.getCodeDirectory(), tupleClassName, "SELECT * FROM $$tester");
	}

	@Test
	public void testCodeThatUsesGeneratedTuple() throws SQLException, IOException {
		String source = Files.readString(Path.of("src/test/resources/TestSQLite_Source01.java"), StandardCharsets.UTF_8);
		var compiler = new ForeignCompilerJava("_TestData");
		var classpath = compiler.getDefaultClassPath() + File.pathSeparatorChar
				+ "_TestData/" + File.pathSeparatorChar
				+ "_TestData/code" + File.pathSeparatorChar
				+ "src/main/java" + File.pathSeparatorChar
				+ "src/test/java";
		var result = compiler.compileForeignCode( classpath,"TestSQLite_Source01", "org.reldb.wrapd.tuples.generated", source);
		if (!result.compiled) {
			System.out.println(result.compilerMessages);
		}
		assertTrue(result.compiled);
	}

	@AfterAll
	public static void teardown() throws SQLException, IOException {
//		DatabaseConfigurationAndSetup.databaseTeardown("[TRDN]");
	}

}

