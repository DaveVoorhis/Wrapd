package org.reldb.wrapd.sqldb;

import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.compiler.DirClassLoader;
import org.reldb.wrapd.compiler.ForeignCompilerJava;
import org.reldb.wrapd.exceptions.ExceptionFatal;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class Helper {

	private final String baseDir;

	public Helper(String baseDir) {
		this.baseDir = baseDir;
		ensureTestDirectoryExists();
	}

	private void ensureTestDirectoryExists() {
		if (!Directory.chkmkdir(baseDir))
			throw new ExceptionFatal("Helper: Unable to create directory for test: " + baseDir);
	}

	private void dropTable(Database database, String prompt, String tableName) {
		try {
			database.updateAll("DROP TABLE " + tableName);
		} catch (SQLException se) {
			System.out.println(prompt + " ERROR: " + se);
		}
	}

	private void destroyDatabase(String prompt, Database database) {
		dropTable(database, prompt, "$$version");
		dropTable(database, prompt, "$$tester");
		Directory.rmAll(getCodeDirectory());
	}

	private void createDatabase(Database database) throws SQLException {
		database.transact(xact -> {
			xact.updateAll("CREATE TABLE $$version (user_db_version INTEGER, framework_db_version INTEGER);");
			xact.updateAll("INSERT INTO $$version VALUES (0, 0);");
			xact.updateAll("CREATE TABLE $$tester (x INTEGER, y INTEGER, PRIMARY KEY (x, y));");
			return true;
		});
	}

	private void destroyTupleClass(String tupleClassName) {
		ResultSetToTuple.destroyTuple(getCodeDirectory(), tupleClassName);
	}

	private void createTupleClass(String tupleClassName, Database database) throws SQLException {
		database.createTupleFromQueryAll(getCodeDirectory(), tupleClassName, "SELECT * FROM $$tester");
	}

	private void setup(String prompt, String tupleClassName, Database database) throws SQLException {
		destroyDatabase(prompt, database);
		createDatabase(database);
		destroyTupleClass(tupleClassName);
		createTupleClass(tupleClassName, database);
	}

	private Class<?> obtainTestCodeClass(String testPackage, String testClassName) throws ClassNotFoundException {
		var dirClassLoader = new DirClassLoader(getCodeDirectory(), testPackage);
		var testClassFullname = testPackage + "." + testClassName;
		return dirClassLoader.forName(testClassFullname);
	}

	private TestExecutionSummary runTestsInClass(Class<?> clazz) {
		var listener = new SummaryGeneratingListener();
		var request = LauncherDiscoveryRequestBuilder.request()
				.selectors(selectClass(clazz))
				.build();
		var launcher = LauncherFactory.create();
		launcher.registerTestExecutionListeners(listener);
		launcher.execute(request);
		return listener.getSummary();
	}

	private ForeignCompilerJava.CompilationResults compileTestCode(String testPackage, String testClassName) throws IOException {
		String source = Files.readString(Path.of("src/test/resources/" + testClassName + ".java"), StandardCharsets.UTF_8);
		var compiler = new ForeignCompilerJava(getCodeDirectory());
		var classpath = compiler.getDefaultClassPath();
		return compiler.compileForeignCode(classpath, testClassName, testPackage, source);
	}

	private void test(String testPackage, String testClassName) throws IOException, ClassNotFoundException {
		var result = compileTestCode(testPackage, testClassName);
		if (!result.compiled) {
			System.out.println(result.compilerMessages);
		}
		assertTrue(result.compiled);

		var clazz = obtainTestCodeClass(testPackage, testClassName);
		assertNotNull(clazz);

		var testExecutionSummary = runTestsInClass(clazz);
		testExecutionSummary.printTo(new PrintWriter(System.out));
		assertEquals(testExecutionSummary.getTotalFailureCount(), 0);
	}

	public String getCodeDirectory() {
		return baseDir + "/code";
	}

	public void test(String prompt,
					 String title,
					 String tupleClassName,
					 String testPackage,
					 String testClassName,
					 Database database) throws ClassNotFoundException, IOException, SQLException {
		System.out.println(prompt + " Executing " + title + " setup.");
		setup(prompt, tupleClassName, database);
		System.out.println(prompt + " Executing " + title + " test.");
		test(testPackage, testClassName);
	}
}
