package org.reldb.wrapd.sqldb;

import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
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

	private void databaseTeardown(String prompt, Database database) {
		dropTable(database, prompt, "$$version");
		dropTable(database, prompt, "$$tester");
		Directory.rmAll(getCodeDirectory());
	}

	private void databaseCreate(Database database) throws SQLException {
		database.transact(xact -> {
			xact.updateAll("CREATE TABLE $$version (user_db_version INTEGER, framework_db_version INTEGER);");
			xact.updateAll("INSERT INTO $$version VALUES (0, 0);");
			xact.updateAll("CREATE TABLE $$tester (x INTEGER, y INTEGER, PRIMARY KEY (x, y));");
			return true;
		});
	}

	private void setup(String prompt, String tupleClassName, Database database) throws SQLException {
		databaseTeardown(prompt, database);
		databaseCreate(database);
		ResultSetToTuple.destroyTuple(getCodeDirectory(), tupleClassName);
		database.createTupleFromQueryAll(getCodeDirectory(), tupleClassName, "SELECT * FROM $$tester");
	}

	private void test(String testPackage, String testClassName) throws IOException, ClassNotFoundException {
		var testClassFullname = testPackage + "." + testClassName;

		// Compile test code
		String source = Files.readString(Path.of("src/test/resources/" + testClassName + ".java"), StandardCharsets.UTF_8);
		var compiler = new ForeignCompilerJava(getCodeDirectory());
		var classpath = compiler.getDefaultClassPath();
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
		var dcl = new DirClassLoader(getCodeDirectory(), testPackage);
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

	public String getBaseDirectory() {
		return baseDir;
	}

	public String getCodeDirectory() {
		return getBaseDirectory() + "/code";
	}

    public void test(String prompt, String title, String tupleClassName, String testPackage, String testClassName, Database database) throws ClassNotFoundException, IOException, SQLException {
		System.out.println(prompt + " Executing " + title + " setup.");
		setup(prompt, tupleClassName, database);
		System.out.println(prompt + " Executing " + title + " test.");
		test(testPackage, testClassName);
	}
}
