package org.reldb.wrapd.database;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.reldb.wrapd.compiler.DirClassLoader;
import org.reldb.wrapd.compiler.ForeignCompilerJava;
import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.sqldb.ResultSetToTuple;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

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
	public void testCodeThatUsesGeneratedTuple() throws IOException, ClassNotFoundException {
		var testClassName = "TestSQLite_Source01";
		var testPackage = "org.reldb.wrapd.tuples.generated";
		var testClassFullname = testPackage + "." + testClassName;

		// Compile test code
		String source = Files.readString(Path.of("src/test/resources/" + testClassName + ".java"), StandardCharsets.UTF_8);
		var compiler = new ForeignCompilerJava(DatabaseConfigurationAndSetup.getCodeDirectory());
		var classpath = compiler.getDefaultClassPath() + File.pathSeparatorChar
				+ "_TestData/code" + File.pathSeparatorChar
				+ "src/main/java" + File.pathSeparatorChar
				+ "src/test/java";
		var result = compiler.compileForeignCode(
				classpath,
				testClassName,
				testPackage,
				source);
		if (!result.compiled) {
			System.out.println(result.compilerMessages);
		}
		assertTrue(result.compiled);

		// Verify that test code class can be loaded
		var dcl = new DirClassLoader(DatabaseConfigurationAndSetup.getCodeDirectory(), testPackage);
		var clazz = dcl.forName(testClassFullname);
		assertNotNull(clazz);

		// Run tests in test code class
		var listener = new SummaryGeneratingListener();
		var request = LauncherDiscoveryRequestBuilder.request()
				.selectors(selectClass(clazz))
				.build();
		var launcher = LauncherFactory.create();
		launcher.registerTestExecutionListeners(listener);
		launcher.execute(request);
		var summary = listener.getSummary();
		summary.printTo(new PrintWriter(System.out));

		assertEquals(summary.getTotalFailureCount(), 0);
	}

}

